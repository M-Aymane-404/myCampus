package com.mycampus.backend.metier.administrateur.imports;


import com.mycampus.backend.dto.administrateur.imports.ErreurImportDTO;
import com.mycampus.backend.dto.administrateur.imports.EtudiantImportDTO;
import com.mycampus.backend.dto.administrateur.imports.RapportImportEtudiantsSortieDTO;
import com.mycampus.backend.dto.administrateur.imports.ResultatParsingImportEtudiantsDTO;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.enumerations.EtatInscription;
import com.mycampus.backend.enumerations.NiveauAnnee;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.repositorys.ClasseRepository;
import com.mycampus.backend.repositorys.EtudiantRepository;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestionImportEtudiantsMetierImpl implements GestionImportEtudiantsMetier {

    private final EtudiantRepository etudiantRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ClasseRepository classeRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int CHUNK_SIZE = 50;
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("csv");

    @Override
    @Transactional
    public RapportImportEtudiantsSortieDTO importerEtudiants(
            MultipartFile fichier, 
            String codeClasse, 
            String modeImport, 
            String motDePasseDefaut
    ) {
        
        log.info("Début import étudiants: classe={}, mode={}", codeClasse, modeImport);
        
        validerFichier(fichier);
        Classe classe = trouverClasse(codeClasse);
        
        ResultatParsingImportEtudiantsDTO resultatParsing = parserFichier(fichier);
        List<EtudiantImportDTO> etudiantsDTO = resultatParsing.getEtudiants();
        List<ErreurImportDTO> erreurs = new ArrayList<>(resultatParsing.getErreurs());
        
        Set<String> emailsVus = new HashSet<>();
        Set<String> cinsVus = new HashSet<>();
        Set<String> apogeesVus = new HashSet<>();
        
        boolean motDePasseGenere = false;
        
        // Charger les données existantes en base pour éviter les appels multiples
        Set<String> emailsExistants = etudiantRepository.findAllEmails();
        Set<String> cinsExistants = utilisateurRepository.findAllCins();
        Set<String> apogeesExistants = etudiantRepository.findAllApogees();
        
        // Inclure les erreurs de parsing dans les lignes avec erreurs
        Set<Integer> lignesAvecErreurs = new HashSet<>();
        for (ErreurImportDTO erreurParsing : resultatParsing.getErreurs()) {
            lignesAvecErreurs.add(erreurParsing.getLigne());
        }
        
        List<Etudiant> etudiantsValides = new ArrayList<>();
        
        for (EtudiantImportDTO dto : etudiantsDTO) {
            List<ErreurImportDTO> erreursLigne = validerLigne(
                    dto, emailsVus, cinsVus, apogeesVus
            );
            
            if (erreursLigne.isEmpty()) {
                String emailNormalise = dto.getEmail().toLowerCase().trim();
                String cinNormalise = dto.getCin().trim();
                String apogeeNormalise = dto.getApogee().trim();
                
                boolean emailExiste = emailsExistants.contains(emailNormalise);
                boolean cinExiste = cinsExistants.contains(cinNormalise);
                boolean apogeeExiste = apogeesExistants.contains(apogeeNormalise);
                
                if (emailExiste) {
                    erreurs.add(ErreurImportDTO.builder()
                            .ligne(dto.getNumeroLigne())
                            .champ("email")
                            .message("Email existe déjà en base")
                            .valeur(dto.getEmail())
                            .build());
                    lignesAvecErreurs.add(dto.getNumeroLigne());
                } else if (cinExiste) {
                    erreurs.add(ErreurImportDTO.builder()
                            .ligne(dto.getNumeroLigne())
                            .champ("cin")
                            .message("CIN existe déjà en base")
                            .valeur(dto.getCin())
                            .build());
                    lignesAvecErreurs.add(dto.getNumeroLigne());
                } else if (apogeeExiste) {
                    erreurs.add(ErreurImportDTO.builder()
                            .ligne(dto.getNumeroLigne())
                            .champ("apogee")
                            .message("Apogée existe déjà en base")
                            .valeur(dto.getApogee())
                            .build());
                    lignesAvecErreurs.add(dto.getNumeroLigne());
                } else {
                    Etudiant etudiant = creerEtudiant(dto, classe);
                    etudiantsValides.add(etudiant);
                    
                    emailsVus.add(emailNormalise);
                    cinsVus.add(cinNormalise);
                    apogeesVus.add(apogeeNormalise);
                }
            } else {
                erreurs.addAll(erreursLigne);
                lignesAvecErreurs.add(dto.getNumeroLigne());
            }
        }
        
        if ("ATOMIC".equalsIgnoreCase(modeImport) && !lignesAvecErreurs.isEmpty()) {
            throw new UtilisateursException("Import atomique échoué: " + lignesAvecErreurs.size() + " lignes invalides");
        }
        
        int nombreInseres = 0;
        
        if (!etudiantsValides.isEmpty()) {
            sauvegarderEnBatch(etudiantsValides);
            nombreInseres = etudiantsValides.size();
        }
        
        int nombreErreurs = lignesAvecErreurs.size();
        int totalLignes = resultatParsing.getTotalLignesLues();
        
        int nombreIgnores = Math.max(0, totalLignes - nombreInseres - nombreErreurs);
        
        return RapportImportEtudiantsSortieDTO.builder()
                .totalLignes(totalLignes)
                .nombreInseres(nombreInseres)
                .nombreIgnores(nombreIgnores)
                .nombreErreurs(nombreErreurs)
                .motDePasseGenere(false)
                .erreurs(erreurs)
                .build();
    }

    private void validerFichier(MultipartFile fichier) {
        if (fichier.isEmpty()) {
            throw new UtilisateursException("Le fichier ne peut pas être vide");
        }
        
        String nomFichier = fichier.getOriginalFilename();
        if (nomFichier == null) {
            throw new UtilisateursException("Nom de fichier invalide");
        }
        
        String extension = nomFichier.substring(nomFichier.lastIndexOf('.') + 1).toLowerCase();
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new UtilisateursException("Format non supporté: utilisez .csv");
        }
    }

    private Classe trouverClasse(String codeClasse) {
        return classeRepository.findByCodeClasse(codeClasse)
                .orElseThrow(() -> new UtilisateursException("Classe non trouvée: " + codeClasse));
    }

    private ResultatParsingImportEtudiantsDTO parserFichier(MultipartFile fichier) {
        return parserCsv(fichier);
    }

    private ResultatParsingImportEtudiantsDTO parserCsv(MultipartFile fichier) {
        List<EtudiantImportDTO> etudiants = new ArrayList<>();
        List<ErreurImportDTO> erreurs = new ArrayList<>();
        
        int totalLignesLues = 0;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fichier.getInputStream()))) {
            String ligne = reader.readLine(); // header
            int numeroLigne = 2;
            
            while ((ligne = reader.readLine()) != null) {
                if (ligne.trim().isEmpty()) {
                    numeroLigne++;
                    continue;
                }
                totalLignesLues++;


                String[] champs = parseCsvLine(ligne);
                if (champs.length < 10) {
                    erreurs.add(ErreurImportDTO.builder()
                            .ligne(numeroLigne)
                            .champ("format")
                            .message("Ligne incomplète: 10 colonnes requises")
                            .valeur(ligne)
                            .build());
                    numeroLigne++;
                    continue;
                }
                
                try {
                    EtudiantImportDTO dto = construireDTO(champs, numeroLigne);
                    etudiants.add(dto);
                } catch (Exception e) {
                    log.warn("Erreur parsing ligne {}: {}", numeroLigne, e.getMessage());
                    erreurs.add(ErreurImportDTO.builder()
                            .ligne(numeroLigne)
                            .champ("parsing")
                            .message("Erreur de parsing: " + e.getMessage())
                            .valeur(ligne)
                            .build());
                }
                numeroLigne++;
            }
        } catch (Exception e) {
            throw new UtilisateursException("Erreur lors de la lecture du fichier CSV: " + e.getMessage());
        }
        
        return ResultatParsingImportEtudiantsDTO.builder()
                .etudiants(etudiants)
                .erreurs(erreurs)
                .totalLignesLues(totalLignesLues)
                .build();
    }

    
    private String[] parseCsvLine(String ligne) {
        List<String> champs = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < ligne.length(); i++) {
            char c = ligne.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                champs.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        champs.add(current.toString().trim());
        return champs.toArray(new String[0]);
    }

    private EtudiantImportDTO construireDTO(String[] champs, int numeroLigne) {
        Semestre semestreActuel = null;
        try {
            semestreActuel = Semestre.valueOf(champs[8].trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Valeur Semestre invalide: " + champs[8].trim());
        }
        
        EtatInscription etatInscription = null;
        try {
            etatInscription = EtatInscription.valueOf(champs[9].trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Valeur EtatInscription invalide: " + champs[9].trim());
        }
        
        Integer anneeInscription = null;
        try {
            anneeInscription = Integer.parseInt(champs[7].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valeur anneeInscription invalide: " + champs[7].trim());
        }
        
        LocalDate dateNaissance = null;
        if (champs.length > 10 && !champs[10].trim().isEmpty()) {
            try {
                dateNaissance = LocalDate.parse(champs[10].trim());
            } catch (Exception e) {
                throw new IllegalArgumentException("Format dateNaissance invalide (yyyy-MM-dd): " + champs[10].trim());
            }
        }
        
        return EtudiantImportDTO.builder()
                .numeroLigne(numeroLigne)
                .apogee(champs[0].trim())
                .cin(champs[1].trim())
                .nom(champs[2].trim())
                .prenom(champs[3].trim())
                .email(champs[4].trim().toLowerCase())
                .tel(champs.length > 5 && !champs[5].trim().isEmpty() ? champs[5].trim() : null)
                .niveau(NiveauAnnee.fromValeur(champs[6].trim()))
                .anneeInscription(anneeInscription)
                .semestreActuel(semestreActuel)
                .etatInscription(etatInscription)
                .dateNaissance(dateNaissance)
                .build();
    }

    private List<ErreurImportDTO> validerLigne(
            EtudiantImportDTO dto,
            Set<String> emailsVus, Set<String> cinsVus, Set<String> apogeesVus
    ) {
        List<ErreurImportDTO> erreurs = new ArrayList<>();
        
        if (dto.getApogee() == null || dto.getApogee().trim().isEmpty()) {
            erreurs.add(ErreurImportDTO.builder()
                    .ligne(dto.getNumeroLigne())
                    .champ("apogee")
                    .message("Champ obligatoire")
                    .valeur(dto.getApogee())
                    .build());
        }
        
        if (dto.getCin() == null || dto.getCin().trim().isEmpty()) {
            erreurs.add(ErreurImportDTO.builder()
                    .ligne(dto.getNumeroLigne())
                    .champ("cin")
                    .message("Champ obligatoire")
                    .valeur(dto.getCin())
                    .build());
        }
        
        if (dto.getNom() == null || dto.getNom().trim().isEmpty()) {
            erreurs.add(ErreurImportDTO.builder()
                    .ligne(dto.getNumeroLigne())
                    .champ("nom")
                    .message("Champ obligatoire")
                    .valeur(dto.getNom())
                    .build());
        }
        
        if (dto.getPrenom() == null || dto.getPrenom().trim().isEmpty()) {
            erreurs.add(ErreurImportDTO.builder()
                    .ligne(dto.getNumeroLigne())
                    .champ("prenom")
                    .message("Champ obligatoire")
                    .valeur(dto.getPrenom())
                    .build());
        }
        
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty() || !dto.getEmail().contains("@")) {
            erreurs.add(ErreurImportDTO.builder()
                    .ligne(dto.getNumeroLigne())
                    .champ("email")
                    .message("Email invalide")
                    .valeur(dto.getEmail())
                    .build());
        }
        
        if (dto.getNiveau() == null ) {
            erreurs.add(ErreurImportDTO.builder()
                    .ligne(dto.getNumeroLigne())
                    .champ("niveau")
                    .message("Champ obligatoire")
                    .valeur("")
                    .build());
        }
        
        if (dto.getAnneeInscription() == null) {
            erreurs.add(ErreurImportDTO.builder()
                    .ligne(dto.getNumeroLigne())
                    .champ("anneeInscription")
                    .message("Champ obligatoire")
                    .valeur(String.valueOf(dto.getAnneeInscription()))
                    .build());
        }
        
        if (dto.getSemestreActuel() == null) {
            erreurs.add(ErreurImportDTO.builder()
                    .ligne(dto.getNumeroLigne())
                    .champ("semestreActuel")
                    .message("Champ obligatoire")
                    .valeur(String.valueOf(dto.getSemestreActuel()))
                    .build());
        }
        
        if (dto.getEtatInscription() == null) {
            erreurs.add(ErreurImportDTO.builder()
                    .ligne(dto.getNumeroLigne())
                    .champ("etatInscription")
                    .message("Champ obligatoire")
                    .valeur(String.valueOf(dto.getEtatInscription()))
                    .build());
        }
        
        String emailNormalise = dto.getEmail().toLowerCase().trim();
        if (emailsVus.contains(emailNormalise)) {
            erreurs.add(ErreurImportDTO.builder()
                    .ligne(dto.getNumeroLigne())
                    .champ("email")
                    .message("Doublon dans le fichier")
                    .valeur(dto.getEmail())
                    .build());
        }
        
        String cinNormalise = dto.getCin().trim();
        if (cinsVus.contains(cinNormalise)) {
            erreurs.add(ErreurImportDTO.builder()
                    .ligne(dto.getNumeroLigne())
                    .champ("cin")
                    .message("Doublon dans le fichier")
                    .valeur(dto.getCin())
                    .build());
        }
        
        String apogeeNormalise = dto.getApogee().trim();
        if (apogeesVus.contains(apogeeNormalise)) {
            erreurs.add(ErreurImportDTO.builder()
                    .ligne(dto.getNumeroLigne())
                    .champ("apogee")
                    .message("Doublon dans le fichier")
                    .valeur(dto.getApogee())
                    .build());
        }
        
        return erreurs;
    }

    private Etudiant creerEtudiant(EtudiantImportDTO dto, Classe classe) {
        String motDePasseHash = passwordEncoder.encode(dto.getApogee().trim());
        
        return Etudiant.builder()
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .email(dto.getEmail())
                .password(motDePasseHash)
                .cin(dto.getCin())
                .tel(dto.getTel())
                .active(true)
                .dateCreation(java.time.LocalDateTime.now())
                .apogee(dto.getApogee())
                .niveau(dto.getNiveau())
                .anneeInscription(dto.getAnneeInscription())
                .semestreActuel(dto.getSemestreActuel())
                .etatInscription(dto.getEtatInscription())
                .dateNaissance(dto.getDateNaissance())
                .classe(classe)
                .build();
    }

    
    private void sauvegarderEnBatch(List<Etudiant> etudiants) {
        for (int i = 0; i < etudiants.size(); i += CHUNK_SIZE) {
            int fin = Math.min(i + CHUNK_SIZE, etudiants.size());
            List<Etudiant> chunk = etudiants.subList(i, fin);
            
            etudiantRepository.saveAll(chunk);
            
            if (i % (CHUNK_SIZE * 2) == 0) {
                etudiantRepository.flush();
            }
        }
    }

}
