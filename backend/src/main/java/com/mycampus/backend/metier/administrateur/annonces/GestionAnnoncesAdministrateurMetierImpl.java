package com.mycampus.backend.metier.administrateur.annonces;

import com.mycampus.backend.dto.Notification.NotificationEntrerPourPlusieursUtilisateurDTO;
import com.mycampus.backend.dto.annonces.AnnonceEntrerDTO;
import com.mycampus.backend.dto.annonces.AnnonceSortieDTO;
import com.mycampus.backend.entites.Annonce;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Departement;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.enumerations.ReferenceType;
import com.mycampus.backend.enumerations.TypeNotification;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.mappers.annonce.AnnonceMapper;
import com.mycampus.backend.metier.Notification.NotificationMetier;
import com.mycampus.backend.metier.annonces.ressources.GestionRessourcesAnnonceMetier;
import com.mycampus.backend.repositorys.AnnonceRepository;
import com.mycampus.backend.repositorys.ClasseRepository;
import com.mycampus.backend.repositorys.DepartementRepository;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GestionAnnoncesAdministrateurMetierImpl implements GestionAnnoncesAdministrateurMetier {

    private final AnnonceRepository annonceRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ClasseRepository classeRepository;
    private final AnnonceMapper annonceMapper;
    private final DepartementRepository departementRepository;
    private final NotificationMetier notificationMetier;
    private final GestionRessourcesAnnonceMetier gestionRessourcesAnnonceMetier;

    @Override
    @Transactional
    public AnnonceSortieDTO creerAnnonce(AnnonceEntrerDTO annonceDTO, Long idAuteur) {
        Utilisateur auteur = utilisateurRepository.findById(idAuteur)
                .orElseThrow(() -> new UtilisateursException("Auteur non trouve"));

        Classe classe = null;
        Departement departement = null;

        switch (annonceDTO.getCibleType()) {
            case ETUDIANTS_CLASSE -> {
                if (annonceDTO.getCodeClasse() == null || annonceDTO.getCodeClasse().isBlank()) {
                    throw new UtilisateursException("Code classe obligatoire pour ETUDIANTS_CLASSE");
                }
                classe = classeRepository.findByCodeClasse(annonceDTO.getCodeClasse())
                        .orElseThrow(() -> new UtilisateursException("Classe non trouvee: " + annonceDTO.getCodeClasse()));
            }
            case PROFESSEURS_DEPARTEMENT -> {
                if (annonceDTO.getCodeDepartement() == null || annonceDTO.getCodeDepartement().isBlank()) {
                    throw new UtilisateursException("Code departement obligatoire pour PROFESSEURS_DEPARTEMENT");
                }
                departement = departementRepository.findByCodeDepartement(annonceDTO.getCodeDepartement())
                        .orElseThrow(() -> new UtilisateursException("Departement non trouve: " + annonceDTO.getCodeDepartement()));
            }
            default -> {
            }
        }

        Annonce annonce = Annonce.builder()
                .titre(annonceDTO.getTitre())
                .contenu(annonceDTO.getContenu())
                .datePublication(LocalDateTime.now())
                .publiee(true)
                .typeAnnonce(annonceDTO.getTypeAnnonce())
                .cibleType(annonceDTO.getCibleType())
                .auteur(auteur)
                .classe(classe)
                .departement(departement)
                .build();

        Annonce savedAnnonce = annonceRepository.save(annonce);
        notifierAnnoncePubliee(savedAnnonce, auteur.getId());

        log.info("Annonce creee avec succes: {}", savedAnnonce.getId());
        return annonceMapper.changerVersSortieDTO(savedAnnonce);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnonceSortieDTO> listerToutesAnnonces() {
        log.info("Listage de toutes les annonces");
        return annonceRepository.findAllWithAuteurAndClasse().stream()
                .map(annonceMapper::changerVersSortieDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnonceSortieDTO> listerAnnoncesParClasse(String codeClasse) {
        log.info("Listage des annonces pour la classe: {}", codeClasse);
        return annonceRepository.findByClasse_CodeClasse(codeClasse).stream()
                .map(annonceMapper::changerVersSortieDTO)
                .toList();
    }

    @Override
    @Transactional
    public void supprimerAnnonce(Long annonceId) {
        log.info("Suppression de l'annonce: {}", annonceId);

        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new UtilisateursException("Annonce non trouvee"));

        gestionRessourcesAnnonceMetier.supprimerRessourcesSilencieusement(annonce);
        annonceRepository.delete(annonce);
        log.info("Annonce supprimee avec succes");
    }

    @Override
    @Transactional
    public AnnonceSortieDTO publierAnnonce(Long annonceId) {
        log.info("Publication de l'annonce: {}", annonceId);

        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new UtilisateursException("Annonce non trouvee"));

        annonce.setPubliee(true);
        Annonce savedAnnonce = annonceRepository.save(annonce);
        notifierAnnoncePubliee(savedAnnonce, annonce.getAuteur().getId());
        log.info("Annonce publiee avec succes");

        return annonceMapper.changerVersSortieDTO(savedAnnonce);
    }

    @Override
    @Transactional
    public AnnonceSortieDTO depublierAnnonce(Long annonceId) {
        log.info("Depublication de l'annonce: {}", annonceId);

        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new UtilisateursException("Annonce non trouvee"));

        annonce.setPubliee(false);
        Annonce savedAnnonce = annonceRepository.save(annonce);
        log.info("Annonce depubliee avec succes");

        return annonceMapper.changerVersSortieDTO(savedAnnonce);
    }

    private void notifierAnnoncePubliee(Annonce annonce, Long auteurId) {
        NotificationEntrerPourPlusieursUtilisateurDTO dto = NotificationEntrerPourPlusieursUtilisateurDTO.builder()
                .type(TypeNotification.ANNONCE_PUBLIEE)
                .auteurId(auteurId)
                .titre(annonce.getTitre())
                .message("Nouvelle annonce: " + annonce.getTitre())
                .referenceType(ReferenceType.ANNONCE)
                .referenceId(annonce.getId())
                .build();

        switch (annonce.getCibleType()) {
            case ETUDIANTS_CLASSE -> {
                if (annonce.getClasse() != null) {
                    notificationMetier.notifierEtudiantClasse(annonce.getClasse().getCodeClasse(), dto);
                }
            }
            case PROFESSEURS_DEPARTEMENT -> {
                if (annonce.getDepartement() != null) {
                    notificationMetier.notifierProfesseurDepartement(annonce.getDepartement().getCodeDepartement(), dto);
                }
            }
            case ETUDIANTS_GLOBAL -> notificationMetier.notifierToutsEtudiant(dto);
            case PROFESSEURS_GLOBAL -> notificationMetier.notifierToutsProfesseur(dto);
        }
    }
}
