package com.mycampus.backend.metier.annonces.ressources;

import com.mycampus.backend.dto.annonces.RessourceAnnonceSortieDTO;
import com.mycampus.backend.dto.annonces.TelechargementRessourceAnnonceSortieDTO;
import com.mycampus.backend.entites.Annonce;
import com.mycampus.backend.entites.Departement;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.entites.RessourceAnnonce;
import com.mycampus.backend.enumerations.AnnonceCibleType;
import com.mycampus.backend.enumerations.ReferenceType;
import com.mycampus.backend.enumerations.TypeNotification;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.metier.professeur.StockageFichiersService;
import com.mycampus.backend.metier.security.ValidationFichierSecuriseService;
import com.mycampus.backend.repositorys.AnnonceRepository;
import com.mycampus.backend.repositorys.EtudiantRepository;
import com.mycampus.backend.repositorys.NotificationRepository;
import com.mycampus.backend.repositorys.ProfesseurRepository;
import com.mycampus.backend.repositorys.RessourceAnnonceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GestionRessourcesAnnonceMetierImpl implements GestionRessourcesAnnonceMetier {

    private final AnnonceRepository annonceRepository;
    private final RessourceAnnonceRepository ressourceAnnonceRepository;
    private final ProfesseurRepository professeurRepository;
    private final EtudiantRepository etudiantRepository;
    private final NotificationRepository notificationRepository;
    private final StockageFichiersService stockageFichiersService;

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
        requireAnnonceVisiblePourProfesseur(annonceId, emailProfesseur);
        return mapperListe(ressourceAnnonceRepository.findByAnnonce_IdOrderByDateUploadDesc(annonceId));
    }

    @Override
    @Transactional(readOnly = true)
    public TelechargementRessourceAnnonceSortieDTO preparerTelechargementProfesseur(Long ressourceId, String emailProfesseur) {
        requireEmailProfesseur(emailProfesseur);
        RessourceAnnonce ressource = requireRessource(ressourceId);
        requireAnnonceVisiblePourProfesseur(ressource.getAnnonce().getId(), emailProfesseur);
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

    
    private Annonce requireAnnonceVisiblePourProfesseur(Long annonceId, String emailProfesseur) {
        requireEmailProfesseur(emailProfesseur);

        Professeur professeur = professeurRepository.findByEmail(emailProfesseur)
                .orElseThrow(() -> new UtilisateursException("Professeur non trouve"));

        Annonce annonce = requireAnnonce(annonceId);

        if (annonce.getAuteur() != null && professeur.getId().equals(annonce.getAuteur().getId())) {
            return annonce;
        }

        AnnonceCibleType cibleType = annonce.getCibleType();
        if (cibleType == AnnonceCibleType.PROFESSEURS_GLOBAL) {
            return annonce;
        }

        if (cibleType == AnnonceCibleType.PROFESSEURS_DEPARTEMENT) {
            if (estAnnonceDepartementVisiblePourProfesseur(annonce, professeur)) {
                return annonce;
            }
        }

        if (cibleType == AnnonceCibleType.ETUDIANTS_GLOBAL ||
                cibleType == AnnonceCibleType.ETUDIANTS_CLASSE) {
            return annonce;
        }

        throw new UtilisateursException("Annonce non accessible");
    }

    private boolean estAnnonceDepartementVisiblePourProfesseur(Annonce annonce, Professeur professeur) {
        if (annonce.getAuteur() != null && professeur.getId().equals(annonce.getAuteur().getId())) {
            return true;
        }

        boolean annonceAUnCiblageNotification = notificationRepository.existsByTypeAndReferenceTypeAndReferenceId(
                TypeNotification.ANNONCE_PUBLIEE,
                ReferenceType.ANNONCE,
                annonce.getId()
        );

        if (!annonceAUnCiblageNotification) {
            Departement depAnnonce = annonce.getDepartement();
            Departement depProf = professeur.getDepartementOuTravail();
            return depAnnonce != null
                    && depProf != null
                    && depAnnonce.getCodeDepartement().equals(depProf.getCodeDepartement());
        }

        return notificationRepository.existsByUtilisateur_IdAndTypeAndReferenceTypeAndReferenceId(
                professeur.getId(),
                TypeNotification.ANNONCE_PUBLIEE,
                ReferenceType.ANNONCE,
                annonce.getId()
        );
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
        StockageFichiersService.FichierStocke fichierStocke = stockageFichiersService.sauvegarderFichier(
                fichier,
                "annonces",
                Set.of(
                        ValidationFichierSecuriseService.FormatFichierAutorise.PDF,
                        ValidationFichierSecuriseService.FormatFichierAutorise.JPEG,
                        ValidationFichierSecuriseService.FormatFichierAutorise.PNG
                ),
                10 * 1024 * 1024L
        );

        RessourceAnnonce ressource = RessourceAnnonce.builder()
                .annonce(annonce)
                .nomOriginal(fichierStocke.nomOriginal())
                .chemin(fichierStocke.cheminRelatif())
                .typeMime(fichierStocke.typeMime())
                .taille(fichierStocke.taille())
                .dateUpload(LocalDateTime.now())
                .build();

        RessourceAnnonce sauvegardee = ressourceAnnonceRepository.save(ressource);
        return versSortieDTO(sauvegardee);
    }

    private void supprimerFichierSurDisqueSilencieusement(String chemin) {
        if (!StringUtils.hasText(chemin)) {
            return;
        }
        stockageFichiersService.supprimerSilencieusement(chemin);
    }

    private TelechargementRessourceAnnonceSortieDTO versTelechargementDTO(RessourceAnnonce ressource) {
        String chemin = ressource.getChemin();
        if (!StringUtils.hasText(chemin)) {
            throw new UtilisateursException("Chemin de la ressource introuvable");
        }

        stockageFichiersService.chargerCommeResource(chemin);

        return TelechargementRessourceAnnonceSortieDTO.builder()
                .id(ressource.getId())
                .nomOriginal(ressource.getNomOriginal())
                .typeMime(ressource.getTypeMime())
                .taille(ressource.getTaille())
                .chemin(chemin)
                .build();
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
