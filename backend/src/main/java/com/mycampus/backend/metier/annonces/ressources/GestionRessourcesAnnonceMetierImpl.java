package com.mycampus.backend.metier.annonces.ressources;

import com.mycampus.backend.dto.annonces.RessourceAnnonceSortieDTO;
import com.mycampus.backend.dto.annonces.TelechargementRessourceAnnonceSortieDTO;
import com.mycampus.backend.entites.Annonce;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.entites.RessourceAnnonce;
import com.mycampus.backend.enumerations.AnnonceCibleType;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.repositorys.AnnonceRepository;
import com.mycampus.backend.repositorys.EtudiantRepository;
import com.mycampus.backend.repositorys.ProfesseurRepository;
import com.mycampus.backend.repositorys.RessourceAnnonceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GestionRessourcesAnnonceMetierImpl implements GestionRessourcesAnnonceMetier {

    private final AnnonceRepository annonceRepository;
    private final RessourceAnnonceRepository ressourceAnnonceRepository;
    private final ProfesseurRepository professeurRepository;
    private final EtudiantRepository etudiantRepository;

    @Override
    public List<RessourceAnnonceSortieDTO> ajouterRessourcesAdmin(Long annonceId, List<MultipartFile> fichiers) {
        Annonce annonce = requireAnnonce(annonceId);
        return sauvegarderRessources(annonce, fichiers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RessourceAnnonceSortieDTO> listerRessourcesAdmin(Long annonceId) {
        requireAnnonce(annonceId);
        return mapperListe(ressourceAnnonceRepository.findByAnnonce_IdOrderByDateUploadDesc(annonceId));
    }

    @Override
    @Transactional(readOnly = true)
    public TelechargementRessourceAnnonceSortieDTO preparerTelechargementAdmin(Long ressourceId) {
        return versTelechargementDTO(requireRessource(ressourceId));
    }

    @Override
    public List<RessourceAnnonceSortieDTO> ajouterRessourcesProfesseur(Long annonceId, String emailProfesseur, List<MultipartFile> fichiers) {
        Annonce annonce = requireAnnonceProfesseur(annonceId, emailProfesseur);
        return sauvegarderRessources(annonce, fichiers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RessourceAnnonceSortieDTO> listerRessourcesProfesseur(Long annonceId, String emailProfesseur) {
        requireAnnonceProfesseur(annonceId, emailProfesseur);
        return mapperListe(ressourceAnnonceRepository.findByAnnonce_IdOrderByDateUploadDesc(annonceId));
    }

    @Override
    @Transactional(readOnly = true)
    public TelechargementRessourceAnnonceSortieDTO preparerTelechargementProfesseur(Long ressourceId, String emailProfesseur) {
        requireEmailProfesseur(emailProfesseur);
        professeurRepository.findByEmail(emailProfesseur)
                .orElseThrow(() -> new UtilisateursException("Professeur non trouve"));

        RessourceAnnonce ressource = ressourceAnnonceRepository.findByIdAndAnnonce_Auteur_Email(ressourceId, emailProfesseur)
                .orElseThrow(() -> new UtilisateursException("Ressource d'annonce non trouvee ou non autorisee"));

        return versTelechargementDTO(ressource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RessourceAnnonceSortieDTO> listerRessourcesEtudiant(Long annonceId, Long idEtudiant) {
        requireAnnonceVisiblePourEtudiant(annonceId, idEtudiant);
        return mapperListe(ressourceAnnonceRepository.findByAnnonce_IdOrderByDateUploadDesc(annonceId));
    }

    @Override
    @Transactional(readOnly = true)
    public TelechargementRessourceAnnonceSortieDTO preparerTelechargementEtudiant(Long ressourceId, Long idEtudiant) {
        RessourceAnnonce ressource = requireRessource(ressourceId);
        requireAnnonceVisiblePourEtudiant(ressource.getAnnonce().getId(), idEtudiant);
        return versTelechargementDTO(ressource);
    }

    @Override
    public void supprimerRessourcesSilencieusement(Annonce annonce) {
        if (annonce == null || annonce.getRessources() == null) {
            return;
        }

        for (RessourceAnnonce ressource : annonce.getRessources()) {
            supprimerFichierSurDisqueSilencieusement(ressource.getChemin());
        }
    }

    private Annonce requireAnnonce(Long annonceId) {
        if (annonceId == null) {
            throw new UtilisateursException("Annonce non trouvee");
        }

        return annonceRepository.findById(annonceId)
                .orElseThrow(() -> new UtilisateursException("Annonce non trouvee"));
    }

    private Annonce requireAnnonceProfesseur(Long annonceId, String emailProfesseur) {
        requireEmailProfesseur(emailProfesseur);

        Professeur professeur = professeurRepository.findByEmail(emailProfesseur)
                .orElseThrow(() -> new UtilisateursException("Professeur non trouve"));

        Annonce annonce = requireAnnonce(annonceId);
        if (!annonce.getAuteur().getId().equals(professeur.getId())) {
            throw new UtilisateursException("Vous ne pouvez gerer que les ressources de vos propres annonces");
        }

        return annonce;
    }

    private Annonce requireAnnonceVisiblePourEtudiant(Long annonceId, Long idEtudiant) {
        if (idEtudiant == null) {
            throw new UtilisateursException("Etudiant non trouve");
        }

        Etudiant etudiant = etudiantRepository.findById(idEtudiant)
                .orElseThrow(() -> new UtilisateursException("Etudiant non trouve"));

        Annonce annonce = requireAnnonce(annonceId);
        if (!annonce.isPubliee()) {
            throw new UtilisateursException("Annonce non publiee");
        }

        if (annonce.getCibleType() == AnnonceCibleType.ETUDIANTS_GLOBAL) {
            return annonce;
        }

        if (annonce.getCibleType() == AnnonceCibleType.ETUDIANTS_CLASSE
                && annonce.getClasse() != null
                && etudiant.getClasse() != null
                && annonce.getClasse().getId().equals(etudiant.getClasse().getId())) {
            return annonce;
        }

        throw new UtilisateursException("Annonce non accessible");
    }

    private RessourceAnnonce requireRessource(Long ressourceId) {
        if (ressourceId == null) {
            throw new UtilisateursException("Ressource d'annonce non trouvee");
        }

        return ressourceAnnonceRepository.findById(ressourceId)
                .orElseThrow(() -> new UtilisateursException("Ressource d'annonce non trouvee"));
    }

    private void requireEmailProfesseur(String emailProfesseur) {
        if (!StringUtils.hasText(emailProfesseur)) {
            throw new UtilisateursException("Email professeur obligatoire");
        }
    }

    private List<RessourceAnnonceSortieDTO> sauvegarderRessources(Annonce annonce, List<MultipartFile> fichiers) {
        if (fichiers == null || fichiers.isEmpty()) {
            throw new UtilisateursException("Au moins un fichier est obligatoire");
        }

        return fichiers.stream()
                .filter(fichier -> fichier != null && !fichier.isEmpty())
                .map(fichier -> sauvegarderRessource(annonce, fichier))
                .toList();
    }

    private RessourceAnnonceSortieDTO sauvegarderRessource(Annonce annonce, MultipartFile fichier) {
        String chemin = sauvegarderFichierSurDisque(fichier);

        RessourceAnnonce ressource = RessourceAnnonce.builder()
                .annonce(annonce)
                .nomOriginal(StringUtils.hasText(fichier.getOriginalFilename()) ? fichier.getOriginalFilename() : "fichier")
                .chemin(chemin)
                .typeMime(StringUtils.hasText(fichier.getContentType()) ? fichier.getContentType() : "application/octet-stream")
                .taille(fichier.getSize())
                .dateUpload(LocalDateTime.now())
                .build();

        RessourceAnnonce sauvegardee = ressourceAnnonceRepository.save(ressource);
        return versSortieDTO(sauvegardee);
    }

    private String sauvegarderFichierSurDisque(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new UtilisateursException("Le fichier est obligatoire");
        }

        try {
            Path dossierBase = Paths.get("uploads", "annonces").toAbsolutePath().normalize();
            Files.createDirectories(dossierBase);

            String nomOriginal = fichier.getOriginalFilename() != null ? fichier.getOriginalFilename() : "fichier";
            nomOriginal = StringUtils.cleanPath(nomOriginal);

            String extension = "";
            int index = nomOriginal.lastIndexOf('.');
            if (index > 0 && index < nomOriginal.length() - 1) {
                extension = nomOriginal.substring(index);
            }

            Path destination = dossierBase.resolve(UUID.randomUUID() + extension).normalize();
            if (!destination.startsWith(dossierBase)) {
                throw new UtilisateursException("Nom de fichier invalide");
            }

            fichier.transferTo(destination.toFile());
            return destination.toString();
        } catch (UtilisateursException e) {
            throw e;
        } catch (Exception e) {
            throw new UtilisateursException("Erreur lors du stockage de la ressource: " + e.getMessage());
        }
    }

    private void supprimerFichierSurDisqueSilencieusement(String chemin) {
        if (!StringUtils.hasText(chemin)) {
            return;
        }

        try {
            Files.deleteIfExists(Path.of(chemin));
        } catch (Exception e) {
            log.warn("Suppression disque ignoree pour la ressource annonce: {}", chemin);
        }
    }

    private TelechargementRessourceAnnonceSortieDTO versTelechargementDTO(RessourceAnnonce ressource) {
        String chemin = ressource.getChemin();
        if (!StringUtils.hasText(chemin)) {
            throw new UtilisateursException("Chemin de la ressource introuvable");
        }

        try {
            Path cheminNormalise = Paths.get(chemin).toAbsolutePath().normalize();
            if (!Files.exists(cheminNormalise) || !Files.isReadable(cheminNormalise)) {
                throw new UtilisateursException("Fichier introuvable sur le disque");
            }

            return TelechargementRessourceAnnonceSortieDTO.builder()
                    .id(ressource.getId())
                    .nomOriginal(ressource.getNomOriginal())
                    .typeMime(ressource.getTypeMime())
                    .taille(ressource.getTaille())
                    .chemin(cheminNormalise.toString())
                    .build();
        } catch (InvalidPathException e) {
            throw new UtilisateursException("Chemin de la ressource invalide");
        }
    }

    private List<RessourceAnnonceSortieDTO> mapperListe(List<RessourceAnnonce> ressources) {
        List<RessourceAnnonce> safeRessources = ressources != null ? ressources : Collections.emptyList();
        return safeRessources.stream().map(this::versSortieDTO).toList();
    }

    private RessourceAnnonceSortieDTO versSortieDTO(RessourceAnnonce ressource) {
        return RessourceAnnonceSortieDTO.builder()
                .id(ressource.getId())
                .annonceId(ressource.getAnnonce().getId())
                .nomOriginal(ressource.getNomOriginal())
                .typeMime(ressource.getTypeMime())
                .taille(ressource.getTaille())
                .dateUpload(ressource.getDateUpload())
                .build();
    }
}
