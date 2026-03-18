package com.mycampus.backend.metier.etudiant.StructureEtudiant;

import com.mycampus.backend.dto.etudiant.DonneeEntrer.EtudiantModificationEtudiantDTO;
import com.mycampus.backend.dto.etudiant.DonneeSortie.*;
import com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote.ExamenDeReleveEtudiantDTO;
import com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote.MatiereDeReleveEtudiantDTO;
import com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote.ModuleDeReleveEtudiantDTO;
import com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote.ReleveDeNoteEtudiantParSessionDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.fichiersSeance.TelechargementFichierSeanceSortieDTO;
import com.mycampus.backend.entites.*;
import com.mycampus.backend.entites.Module;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.StatusAbsence;
import com.mycampus.backend.enumerations.TypeExamen;
import com.mycampus.backend.enumerations.TypeSeance;
import com.mycampus.backend.exceptions.EtudiantException;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.mappers.administrateur.UtilisateursMapper;
import com.mycampus.backend.mappers.etudiant.StructureEtudiantMapper;
import com.mycampus.backend.repositorys.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class GestionStructureEtudiantMetierImpl implements GestionStructureEtudiantMetier {
     private final EtudiantRepository etudiantRepository;
     private final UtilisateurRepository utilisateurRepository;
    private final ModuleRepository moduleRepository;
    private final MatiereRepository matiereRepository;
    private final SeanceRepository seanceRepository;
    private final ClasseRepository classeRepository;
    private final FichierSeanceRepository fichierSeanceRepository;
    private final PresenceRepository presenceRepository;
    private final ResultatRepository resultatRepository;
    private final AffectationEnseignementRepository affectationEnseignementRepository;
    private final ProfesseurRepository professeurRepository;
    private final StructureEtudiantMapper mapper;
    private final UtilisateursMapper utilisateursMapper;
    private final PasswordEncoder passwordEncoder;
    private final ClasseRepository classRepository;

    @Override
    public EtudiantSortieEtudiantDTO trouverEtudiantConnecter(Long id) throws EtudiantException {


        if(id == null){
            throw  new EtudiantException("id est obli");
        }

        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new EtudiantException("Ã‰tudiant introuvable"));

        String codeClasse = null;
        Integer dureeFormation = null;
        if (etudiant.getClasse() != null) {
            codeClasse = etudiant.getClasse().getCodeClasse();
            if (etudiant.getClasse().getFiliere() != null) {
                dureeFormation = etudiant.getClasse().getFiliere().getDureeFormation();
            }
        }

        return EtudiantSortieEtudiantDTO.builder()
                .id(etudiant.getId())
                .nom(etudiant.getNom())
                .prenom(etudiant.getPrenom())
                .email(etudiant.getEmail())
                .cin(etudiant.getCin())
                .tel(etudiant.getTel())
                .photoNom(etudiant.getPhotoNom())
                .photoUrl(etudiant.getPhotoUrl())
                .active(etudiant.getActive())
                .apogee(etudiant.getApogee())
                .niveau(etudiant.getNiveau())
                .anneeInscription(etudiant.getAnneeInscription())
                .semestreActuel(etudiant.getSemestreActuel())
                .anneeFinEtude(etudiant.getAnneeFinEtude())
                .dateNaissance(etudiant.getDateNaissance())
                .etatInscription(etudiant.getEtatInscription())
                .codeClasse(codeClasse)
                .dureeFormation(dureeFormation)
                .nombreSemestres(dureeFormation != null ? dureeFormation * 2 : null)
                .dateDerniereConnexion(etudiant.getDateDerniereConnexion())
                .build();
    }


    @Override
    public EtudiantSortieEtudiantDTO modifierEtudiant(String email, EtudiantModificationEtudiantDTO etudiantModificationDTO) throws UtilisateursException {
        if (email == null || email.trim().isEmpty()) {
            throw new UtilisateursException("L'email de l'Ã©tudiant ne peut pas Ãªtre nulle");
        }

        if (etudiantModificationDTO == null) {
            throw new UtilisateursException("L'Ã©tudiant ne peut pas Ãªtre nul");
        }

        Etudiant etudiantAModifier = etudiantRepository.findByEmail(email).orElseThrow(() ->
                new UtilisateursException("Ã‰tudiant introuvable avec l'apogÃ©e: " + email));








        if(etudiantModificationDTO.getTel() != null && !etudiantModificationDTO.getTel().trim().isEmpty()) {
            if(!etudiantAModifier.getTel().equals(etudiantModificationDTO.getTel()) && utilisateurRepository.existsByTel(etudiantModificationDTO.getTel())){
                throw new UtilisateursException("Un utilisateur avec cet numero existe dÃ©jÃ ");

            }
            etudiantAModifier.setTel(etudiantModificationDTO.getTel());
        }




        if (etudiantModificationDTO.getNouveauPassword() != null && !etudiantModificationDTO.getNouveauPassword().trim().isEmpty()) {

            if (etudiantModificationDTO.getAncienPassword() == null || etudiantModificationDTO.getAncienPassword().trim().isEmpty()) {
                throw new UtilisateursException("Ancien mot de passe obligatoire");
            }

            boolean ok = passwordEncoder.matches(etudiantModificationDTO.getAncienPassword(), etudiantAModifier.getPassword());

            if (!ok) {
                throw new UtilisateursException("Mot de passe incorrect");
            }

            etudiantAModifier.setPassword(passwordEncoder.encode(etudiantModificationDTO.getNouveauPassword()));
        }

        Etudiant etudiantModifie = etudiantRepository.save(etudiantAModifier);
        return mapper.FromEtudientVersEtudiantSortieDTO(etudiantModifie);
    }



    @Override
    @Transactional(readOnly = true)
    public List<ModuleSortieEtudiantDTO> trouverModulesDEtudiantConnecter(Long id) throws EtudiantException {
        if(id == null){
            throw  new EtudiantException("id est obligatoire");
        }

        List<Module> modules = moduleRepository.findByEtudiantId(id);

        return mapper.DuListeModuleVersListModuleSortieEtudiantDTO(modules);

    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleSortieEtudiantDTO> trouverModulesDEtudiantConnecterParSemester(Long id, Semestre semestre) throws EtudiantException {
        if(id == null){
            throw  new EtudiantException("id est obligatoire");
        }
        if(semestre == null){
            List<Module> modules = moduleRepository.findByEtudiantId(id);
            return mapper.DuListeModuleVersListModuleSortieEtudiantDTO(modules);
        }else {
            List<Module> modules = moduleRepository.findByEtudiantIdEtSemester(id,semestre);
            return mapper.DuListeModuleVersListModuleSortieEtudiantDTO(modules);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatiereSortieEtudiantDTO> trouverMatiereParCodeModule(Long etudiantId , String codeModule) throws EtudiantException {
        if(codeModule == null){
            throw  new EtudiantException("code Module  est obligatoire");
        }
        if (etudiantId == null){
            throw  new EtudiantException("id Ã©tudiant est obligatoire");
        }

        List<Matiere> matieres = matiereRepository.findByModule_CodeModule(codeModule);
        Classe classe = classeRepository.findByEtudiant_idEtudiant(etudiantId);
        if(classe == null){
            throw new EtudiantException("Classe introuvable pour l'Ã©tudiant " + etudiantId);
        }

        if(matieres == null){
            return List.of();
        }else {

            return matieres.stream().map(matier -> {
                MatiereSortieEtudiantDTO matiereSortieEtudiantDTO = mapper.DuMatiereVersMatiereSortieEtudiantDTO(matier);
                AffectationEnseignement affectationEnseignement = affectationEnseignementRepository.findByMatiere_CodeMatiereAndClasse_CodeClasse(matier.getCodeMatiere(), classe.getCodeClasse());
                if (affectationEnseignement != null) {
                    matiereSortieEtudiantDTO.setProfesseurDeMatiere(affectationEnseignement.getProfesseur().getNom() + " " + affectationEnseignement.getProfesseur().getPrenom());
                } else {
                    matiereSortieEtudiantDTO.setProfesseurDeMatiere(null);
                }
                return matiereSortieEtudiantDTO;
            }).toList();

        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<SeanceSortieEtudiantDTO> trouverSeanceParCodeMatiereEtFiltrerParTypeSeanceSiExist(Long etudiantId, String codeMatiere, TypeSeance typeSeance) throws EtudiantException {
        if(codeMatiere == null){
            throw  new EtudiantException("code matiÃ¨re est obligatoire");
        }

        if (etudiantId == null){
            throw  new EtudiantException("id d'Ã©tudiant est obligatoire");
        }
        Classe classe = classeRepository.findByEtudiant_idEtudiant(etudiantId);

        List<Seance> seances;
        if (typeSeance == null){
            seances = seanceRepository.findByClasse_CodeClasseAndMatiere_CodeMatiere(classe.getCodeClasse(), codeMatiere);

        }else {
            seances = seanceRepository.findByClasse_CodeClasseAndMatiere_CodeMatiereAndSeance_typeSeance(classe.getCodeClasse(), codeMatiere, typeSeance);
        }
        return mapper.DuListeSeanceVersListeSeanceSortieEtudiantDTO(seances);

    }

    @Override
    @Transactional(readOnly = true)
    public List<FichierSeanceSortieEtudiantDTO> trouverRessourceD_uneSeance(Long seanceId){

            List<FichierSeance> ressources = fichierSeanceRepository.findBySeance_Id(seanceId);

            return mapper.DuListeFichierSeanceVersListeFichierSeanceSortieEtudiantDTO(ressources);
    }




    @Override
    @Transactional(readOnly = true)
    public List<PresenceSortieEtudiantDTO> trouverToutPresenceDuEtudiantConnecter(Long IdEtudiant, StatusAbsence status, LocalDate date) throws EtudiantException {
        if (IdEtudiant == null) {
            throw new EtudiantException("id d'etudiant est obligatoire");
        }

        List<Presence> presences = presenceRepository.findByEtudiant_Id(IdEtudiant);
        if (presences == null || presences.isEmpty()) {
            return List.of();
        }

        if (status != null) {
            presences = presences.stream()
                    .filter(p -> p.getStatus() == status)
                    .toList();
        }

        if (date != null) {
            presences = presences.stream()
                    .filter(p -> p.getSeance() != null && date.equals(p.getSeance().getDate()))
                    .toList();
        }

        return presences.stream()
                .filter(Objects::nonNull)
                .filter(presence -> presence.getSeance() != null)
                .collect(Collectors.toMap(
                        presence -> presence.getSeance().getId(),
                        presence -> presence,
                        this::garderPresenceLaPlusRecente,
                        LinkedHashMap::new
                ))
                .values().stream()
                .sorted(Comparator.comparing(this::extraireDatePresencePourTri).reversed())
                .map(this::construirePresenceSortieEtudiant)
                .toList();
    }

    private Presence garderPresenceLaPlusRecente(Presence gauche, Presence droite) {
        return extraireDatePresencePourTri(gauche).compareTo(extraireDatePresencePourTri(droite)) >= 0 ? gauche : droite;
    }

    private LocalDateTime extraireDatePresencePourTri(Presence presence) {
        if (presence == null) {
            return LocalDateTime.MIN;
        }
        if (presence.getDateDeclaration() != null) {
            return presence.getDateDeclaration();
        }
        if (presence.getSeance() != null && presence.getSeance().getHeureDebut() != null) {
            return presence.getSeance().getHeureDebut();
        }
        if (presence.getSeance() != null && presence.getSeance().getDate() != null) {
            return presence.getSeance().getDate().atStartOfDay();
        }
        return LocalDateTime.MIN;
    }

    private PresenceSortieEtudiantDTO construirePresenceSortieEtudiant(Presence presence) {
        PresenceSortieEtudiantDTO presenceSortieEtudiantDTO = mapper.DuPresenceVersPresenceSortieEtudiantDTO(presence);

        Seance seanceDuPresence = presence.getSeance();
        AffectationEnseignement affectation = seanceDuPresence.getAffectationEnseignement();
        Professeur prof = affectation != null ? affectation.getProfesseur() : null;
        Matiere matiere = affectation != null ? affectation.getMatiere() : null;

        MiniSeanceSortieEtudiantDTO seance = new MiniSeanceSortieEtudiantDTO();
        seance.setId(seanceDuPresence.getId());
        seance.setDate(seanceDuPresence.getDate());
        seance.setSalle(seanceDuPresence.getSalle());
        seance.setType(seanceDuPresence.getType());
        seance.setProf(prof != null ? prof.getNom() + " " + prof.getPrenom() : null);
        seance.setCodeMatiere(matiere != null ? matiere.getCodeMatiere() : null);
        seance.setMatiereNom(matiere != null ? matiere.getNom() : null);

        presenceSortieEtudiantDTO.setSeance(seance);
        return presenceSortieEtudiantDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ReleveDeNoteEtudiantParSessionDTO trouverLeReleverDeNoteParSessionDuEtudiant(Long idEtudiant, Semestre semestre)
            throws EtudiantException {

        if (idEtudiant == null) throw new EtudiantException("id d'etudiant est obligatoire");
        if (semestre == null) throw new EtudiantException("semestre est obligatoire");


        List<Module> modules = moduleRepository.findByEtudiantIdEtSemester(idEtudiant, semestre);

        List<ModuleDeReleveEtudiantDTO> modulesDto = modules.stream()
                .map(mapper::DuModuleVersModuleDeReleveEtudiantDTO)
                .toList();
           AtomicReference<Double> sommeDuMoyennesDesModule = new AtomicReference<>(0.0);
         AtomicInteger nbModulesValider = new AtomicInteger(0);
        AtomicInteger nbModuleQuiOntMoyenneNonNull = new AtomicInteger(0);


        List<Long> examenIds = modulesDto.stream()
                .filter(m -> m.getMatieres() != null)
                .flatMap(m -> m.getMatieres().stream())
                .filter(mat -> mat.getExamens() != null)
                .flatMap(mat -> mat.getExamens().stream())
                .map(ExamenDeReleveEtudiantDTO::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Resultat> resultatByExamenId = examenIds.isEmpty()
                ? Map.of()
                : resultatRepository.findAllByEtudiantIdAndExamenIds(idEtudiant, examenIds).stream()
                .collect(Collectors.toMap(
                        r -> r.getExamen().getId(),
                        r -> r,
                        (a, b) -> { throw new IllegalStateException("Doublon rÃ©sultat pour examen_id=" + a.getExamen().getId()); }
                ));


        modulesDto.forEach(moduleDto -> {
            if (moduleDto.getMatieres() == null) return;

            moduleDto.getMatieres().forEach(matiereDto -> {
                if (matiereDto.getExamens() == null) return;

                // 1) Remplir notes + copie
                matiereDto.getExamens().forEach(examenDto -> {
                    Resultat r = resultatByExamenId.get(examenDto.getId());
                    if (r != null) {
                        examenDto.setNote(r.getNote());
                        examenDto.setCopieEtudiantPath(r.getCopieEtudiantPath());
                    } else {
                        examenDto.setNote(null);
                        examenDto.setCopieEtudiantPath(null);
                    }
                });

                List<ExamenDeReleveEtudiantDTO> examensPasses = matiereDto.getExamens().stream()
                        .filter(examen -> examen.getNote() != null)
                        .sorted(Comparator
                                .comparing(ExamenDeReleveEtudiantDTO::getTypeExamen, Comparator.nullsLast(Enum::compareTo))
                                .thenComparing(ExamenDeReleveEtudiantDTO::getNumeroExamen, Comparator.nullsLast(Integer::compareTo)))
                        .toList();

                matiereDto.setExamens(examensPasses);

                // 2) Calcul moyenne (rÃ¨gle: si rattrapage existe et notÃ© => moyenne = rattrapage,
                // sinon moyenne = average des NORMAL notÃ©s, sinon null)
                Double noteRattrapage = examensPasses.stream()
                        .filter(e -> e.getTypeExamen() == TypeExamen.RATTRAPAGE)
                        .map(ExamenDeReleveEtudiantDTO::getNote)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);

                if (noteRattrapage != null) {
                    matiereDto.setMoyenneDuMatiere(noteRattrapage);

                } else {

                    double moyenneNormal = examensPasses.stream()
                            .filter(e -> e.getTypeExamen() == TypeExamen.NORMAL)
                            .map(ExamenDeReleveEtudiantDTO::getNote)
                            .filter(Objects::nonNull)
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(Double.NaN);

                    matiereDto.setMoyenneDuMatiere(Double.isNaN(moyenneNormal) ? null : moyenneNormal);






                }
                Double moyenne = matiereDto.getMoyenneDuMatiere();
                matiereDto.setValidee(moyenne != null && moyenne >= 10);
            });


            if(moduleDto.getMatieres().size() == 1) {
                moduleDto.setMoyenneDuModule(moduleDto.getMatieres().get(0).getMoyenneDuMatiere());
            }
            if(moduleDto.getMatieres().size() >= 2) {
                double somme = 0.0;
                int nbMoyennes = 0;
                int nbTotal = moduleDto.getMatieres().size();

                for (MatiereDeReleveEtudiantDTO matiere : moduleDto.getMatieres()) {
                    Double moy = matiere.getMoyenneDuMatiere();
                    if (moy != null) {
                        somme += moy;
                        nbMoyennes++;



                    }
                 }

                 moduleDto.setMoyenneDuModule(nbMoyennes == nbTotal ? (somme / nbTotal) : null);
                if(moduleDto.getMoyenneDuModule() > 10){
                    nbModulesValider.getAndIncrement();
                }

            }

           if(moduleDto.getMoyenneDuModule() != null){
               sommeDuMoyennesDesModule.set(sommeDuMoyennesDesModule.get() + moduleDto.getMoyenneDuModule());
               nbModuleQuiOntMoyenneNonNull.getAndIncrement();
           }


            });


        Double moyenneSession = null;
        if(modulesDto.size() == nbModuleQuiOntMoyenneNonNull.get() && nbModuleQuiOntMoyenneNonNull.get() != 0){
            moyenneSession = (sommeDuMoyennesDesModule.get()/nbModuleQuiOntMoyenneNonNull.get());
        }

        return ReleveDeNoteEtudiantParSessionDTO.builder()
                .semestre(semestre)
                .modules(modulesDto)
                .nbModulesValider(nbModulesValider.get())
                .nbModules(modulesDto.size())
                .moyenneSession(moyenneSession)

                .build();

    }

    @Override
     public TelechargementFichierSeanceSortieDTO preparerTelechargementFichierSeance(
            Long idEtudiant ,
            Long fichierSeanceId
    ) throws SeancesProfesseurException {

         requireEtudiantId(idEtudiant);

        FichierSeance fichier = requireFichierSeancePourProfesseur(idEtudiant, fichierSeanceId);

        String chemin = fichier.getChemin();
        if (chemin == null || chemin.isBlank()) {
            throw new SeancesProfesseurException("Chemin du fichier introuvable.");
        }

        try {
            Path cheminNormalise = Paths.get(chemin).toAbsolutePath().normalize();

            if (!Files.exists(cheminNormalise) || !Files.isReadable(cheminNormalise)) {
                throw new SeancesProfesseurException("Fichier introuvable sur le disque.");
            }

            return TelechargementFichierSeanceSortieDTO.builder()
                    .id(fichier.getId())
                    .nomOriginal(fichier.getNomOriginal())
                    .typeMime(fichier.getTypeMime())
                    .taille(fichier.getTaille())
                    .chemin(cheminNormalise.toString())
                    .build();

        } catch (InvalidPathException e) {
            throw new SeancesProfesseurException("Chemin du fichier invalide.");
        }
    }

    private void requireEtudiantEmail(String etudiantEmail) throws UtilisateursException {
        if (etudiantEmail == null || etudiantEmail.isBlank()) {
            throw new UtilisateursException("Le email du professeur est obligatoire.");
        }
    }

    private void requireEtudiantId(Long idEtudiant) throws UtilisateursException {
        if (idEtudiant == null  ) {
            throw new UtilisateursException("Le id du professeur est obligatoire.");
        }
    }

    private FichierSeance requireFichierSeancePourProfesseur(String etudiantEmail, Long fichierSeanceId)
            throws SeancesProfesseurException {

        requireEtudiantEmail(etudiantEmail);

        if (fichierSeanceId == null) {
            throw new SeancesProfesseurException("L'identifiant du fichier est obligatoire.");
        }

       Classe classe =  classeRepository.findByEtudiant_EmailEtudiant(etudiantEmail)
               .orElseThrow(() -> new UtilisateursException("Ã‰tudiant ne Assigne a accan classe introuvable ou non autorisÃ©."));

        return fichierSeanceRepository
                .findByIdAndSeance_AffectationEnseignement_Classe_CodeClasse(fichierSeanceId, classe.getCodeClasse())
                .orElseThrow(() -> new SeancesProfesseurException("Fichier introuvable ou non autorisÃ©."));
    }

    private FichierSeance requireFichierSeancePourProfesseur(Long idEtudiant, Long fichierSeanceId)
            throws SeancesProfesseurException {

        requireEtudiantId(idEtudiant);

        if (fichierSeanceId == null) {
            throw new SeancesProfesseurException("L'identifiant du fichier est obligatoire.");
        }

        Classe classe =  classeRepository.findByEtudiant_idEtudiant(idEtudiant);
        if  (classe == null) {
            throw new UtilisateursException("Ã‰tudiant ne Assigne a accan classe introuvable ou non autorisÃ©.");
        }


        return fichierSeanceRepository
                .findByIdAndSeance_AffectationEnseignement_Classe_CodeClasse(fichierSeanceId, classe.getCodeClasse())
                .orElseThrow(() -> new SeancesProfesseurException("Fichier introuvable ou non autorisÃ©."));
    }





}


