package com.mycampus.backend.metier.administrateur.annonces;

import com.mycampus.backend.dto.Notification.NotificationEntrerDTO;
import com.mycampus.backend.dto.Notification.NotificationEntrerPourPlusieursUtilisateurDTO;
import com.mycampus.backend.dto.annonces.AnnonceEntrerDTO;
import com.mycampus.backend.dto.annonces.AnnonceSortieDTO;
import com.mycampus.backend.entites.Annonce;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.enumerations.ReferenceType;
import com.mycampus.backend.enumerations.TypeNotification;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.mappers.annonce.AnnonceMapper;
import com.mycampus.backend.metier.Notification.NotificationMetier;
import com.mycampus.backend.metier.annonces.ressources.GestionRessourcesAnnonceMetier;
import com.mycampus.backend.repositorys.AnnonceRepository;
import com.mycampus.backend.repositorys.ClasseRepository;
import com.mycampus.backend.repositorys.ProfesseurRepository;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GestionAnnoncesAdministrateurMetierImpl implements GestionAnnoncesAdministrateurMetier {

    private final AnnonceRepository annonceRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ClasseRepository classeRepository;
    private final AnnonceMapper annonceMapper;
    private final ProfesseurRepository professeurRepository;
    private final NotificationMetier notificationMetier;
    private final GestionRessourcesAnnonceMetier gestionRessourcesAnnonceMetier;

    @Override
    @Transactional
    public AnnonceSortieDTO creerAnnonce(AnnonceEntrerDTO annonceDTO, Long idAuteur) {
        Utilisateur auteur = utilisateurRepository.findById(idAuteur)
                .orElseThrow(() -> new UtilisateursException("Auteur non trouve"));

        Classe classe = null;
        List<Long> professeurIdsCibles = resoudreProfesseurIdsCibles(annonceDTO);

        switch (annonceDTO.getCibleType()) {
            case ETUDIANTS_CLASSE -> {
                if (annonceDTO.getCodeClasse() == null || annonceDTO.getCodeClasse().isBlank()) {
                    throw new UtilisateursException("Code classe obligatoire pour ETUDIANTS_CLASSE");
                }
                classe = classeRepository.findByCodeClasse(annonceDTO.getCodeClasse())
                        .orElseThrow(() -> new UtilisateursException("Classe non trouvee: " + annonceDTO.getCodeClasse()));
            }
            case PROFESSEURS_DEPARTEMENT -> {
                if (professeurIdsCibles.isEmpty()) {
                    throw new UtilisateursException("Pour cibler les professeurs, fournir professeurIds ou professeurPprs");
                }
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
                .build();

        Annonce savedAnnonce = annonceRepository.save(annonce);
        notifierAnnoncePubliee(savedAnnonce, auteur.getId(), professeurIdsCibles);

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
        notifierAnnoncePubliee(savedAnnonce, annonce.getAuteur().getId(), List.of());
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

    private void notifierAnnoncePubliee(Annonce annonce, Long auteurId, List<Long> professeurIdsCibles) {
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
                if (!professeurIdsCibles.isEmpty()) {
                    notifierProfesseursSelectionnes(professeurIdsCibles, auteurId, annonce);
                } else if (annonce.getDepartement() != null) {
                    notificationMetier.notifierProfesseurDepartement(annonce.getDepartement().getCodeDepartement(), dto);
                }
            }
            case ETUDIANTS_GLOBAL -> notificationMetier.notifierToutsEtudiant(dto);
            case PROFESSEURS_GLOBAL -> notificationMetier.notifierToutsProfesseur(dto);
        }
    }

    private List<Long> normaliserProfesseurIds(List<Long> professeurIds) {
        if (professeurIds == null || professeurIds.isEmpty()) {
            return List.of();
        }
        return professeurIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<String> normaliserProfesseurPprs(List<String> professeurPprs) {
        if (professeurPprs == null || professeurPprs.isEmpty()) {
            return List.of();
        }
        return professeurPprs.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private List<Long> resoudreProfesseurIdsCibles(AnnonceEntrerDTO annonceDTO) {
        List<Long> ids = new ArrayList<>(normaliserProfesseurIds(annonceDTO.getProfesseurIds()));

        for (String ppr : normaliserProfesseurPprs(annonceDTO.getProfesseurPprs())) {
            Professeur professeur = professeurRepository.findByPpr(ppr)
                    .orElseThrow(() -> new UtilisateursException("Professeur introuvable pour PPR: " + ppr));
            if (!ids.contains(professeur.getId())) {
                ids.add(professeur.getId());
            }
        }

        if (!ids.isEmpty()) {
            List<Professeur> professeursCibles = professeurRepository.findAllById(ids);
            if (professeursCibles.size() != ids.size()) {
                throw new UtilisateursException("Un ou plusieurs professeurs cibles sont introuvables");
            }
        }

        return ids;
    }

    private void notifierProfesseursSelectionnes(List<Long> professeurIdsCibles, Long auteurId, Annonce annonce) {
        for (Long professeurId : professeurIdsCibles) {
            NotificationEntrerDTO notification = NotificationEntrerDTO.builder()
                    .type(TypeNotification.ANNONCE_PUBLIEE)
                    .auteurId(auteurId)
                    .titre(annonce.getTitre())
                    .message("Nouvelle annonce: " + annonce.getTitre())
                    .referenceType(ReferenceType.ANNONCE)
                    .referenceId(annonce.getId())
                    .utilisateurRecoitId(professeurId)
                    .build();
            notificationMetier.notifierUtilisateur(notification);
        }
    }
}
