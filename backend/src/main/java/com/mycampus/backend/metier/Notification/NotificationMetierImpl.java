package com.mycampus.backend.metier.Notification;

import com.mycampus.backend.dto.Notification.DestinataireNotification;
import com.mycampus.backend.dto.Notification.NotificationEntrerDTO;
import com.mycampus.backend.dto.Notification.NotificationEntrerPourPlusieursUtilisateurDTO;
import com.mycampus.backend.dto.Notification.NotificationSortieDTO;
import com.mycampus.backend.entites.Notification;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.exceptions.NotificationException;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.dto.Notification.NotificationApresCommitEvenement;
import com.mycampus.backend.mappers.Notification.NotificationMappers;
import com.mycampus.backend.repositorys.EtudiantRepository;
import com.mycampus.backend.repositorys.NotificationRepository;
import com.mycampus.backend.repositorys.ProfesseurRepository;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationMetierImpl implements NotificationMetier {
    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationMappers notificationMappers;
    private final EtudiantRepository etudiantRepository;
    private final ProfesseurRepository professeurRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final EntityManager entityManager;

    @Override
    public NotificationSortieDTO notifierUtilisateur(NotificationEntrerDTO notificationEntrerDTO) throws NotificationException {
        validate(notificationEntrerDTO);

        Utilisateur utilisateurQuiRecoit = utilisateurRepository.findById(notificationEntrerDTO.getUtilisateurRecoitId())
                .orElseThrow(() -> new UtilisateursException("Utilisateur qui va recevoir la notification est introuvable"));
        Utilisateur auteur = requireAuteur(notificationEntrerDTO.getAuteurId());
        LocalDateTime maintenant = LocalDateTime.now();

        Notification notification = Notification.builder()
                .utilisateur(utilisateurQuiRecoit)
                .titre(notificationEntrerDTO.getTitre())
                .message(notificationEntrerDTO.getMessage())
                .type(notificationEntrerDTO.getType())
                .dateEnvoi(maintenant)
                .auteur(auteur)
                .referenceId(notificationEntrerDTO.getReferenceId())
                .referenceType(notificationEntrerDTO.getReferenceType())
                .lu(false)
                .build();

        notificationRepository.save(notification);

        NotificationSortieDTO notificationSortieDTO = notificationMappers.notificationToNotificationSortieDTO(notification);
        notificationSortieDTO.setNomCompletAuteur(auteur.getNom() + " " + auteur.getPrenom());
        notificationSortieDTO.setNomCompletUtilisateurRecoit(utilisateurQuiRecoit.getNom() + " " + utilisateurQuiRecoit.getPrenom());

        publierDiffusionsApresCommit(List.of(
                new NotificationApresCommitEvenement.NotificationDiffusion(utilisateurQuiRecoit.getEmail(), notificationSortieDTO)
        ));

        return notificationSortieDTO;
    }

    @Override
    public void notifierEtudiantClasse(String codeClasse, NotificationEntrerPourPlusieursUtilisateurDTO dto) {
        validate(dto);

        List<DestinataireNotification> destinataires = etudiantRepository
                .trouverDestinatairesNotificationParCodeClasse(codeClasse)
                .stream()
                .map(p -> new DestinataireNotification(p.getId(), p.getNom(), p.getPrenom(), p.getEmail()))
                .toList();

        diffuserPourDestinataires(dto, destinataires);
    }

    @Override
    public void notifierProfesseurDepartement(String codeDepartement, NotificationEntrerPourPlusieursUtilisateurDTO dto) throws NotificationException {
        validate(dto);

        List<DestinataireNotification> destinataires = professeurRepository
                .trouverDestinatairesNotificationParCodeDepartement(codeDepartement)
                .stream()
                .map(p -> new DestinataireNotification(p.getId(), p.getNom(), p.getPrenom(), p.getEmail()))
                .toList();

        diffuserPourDestinataires(dto, destinataires);
    }

    @Override
    public void notifierToutsProfesseur(NotificationEntrerPourPlusieursUtilisateurDTO dto) throws NotificationException {
        validate(dto);

        List<DestinataireNotification> destinataires = professeurRepository
                .trouverTousDestinatairesNotification()
                .stream()
                .map(p -> new DestinataireNotification(p.getId(), p.getNom(), p.getPrenom(), p.getEmail()))
                .toList();

        diffuserPourDestinataires(dto, destinataires);
    }

    @Override
    public void notifierToutsEtudiant(NotificationEntrerPourPlusieursUtilisateurDTO dto) throws NotificationException {
        validate(dto);

        List<DestinataireNotification> destinataires = etudiantRepository
                .trouverTousDestinatairesNotification()
                .stream()
                .map(p -> new DestinataireNotification(p.getId(), p.getNom(), p.getPrenom(), p.getEmail()))
                .toList();

        diffuserPourDestinataires(dto, destinataires);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationSortieDTO> listerNotificationsUtilisateur(Long id, Pageable pageable) {
        return notificationRepository.trouverInboxParId(id, pageable).map(notificationMappers::mapperProjectionInbox);
    }

    @Override
    @Transactional(readOnly = true)
    public long compterNotificationsNonLuesUtilisateur(Long id) {
        return notificationRepository.countByUtilisateur_IdAndLuFalse(id);
    }

    @Override
    public NotificationSortieDTO marquerNotificationCommeLue(Long id, Long notificationId) {
        if (notificationId == null) {
            throw new NotificationException("L'identifiant de notification est obligatoire");
        }

        notificationRepository.marquerCommeLueParIdUtilisateur(notificationId, id, LocalDateTime.now());

        Notification notification = notificationRepository.findByIdAndUtilisateur_Id(notificationId, id)
                .orElseThrow(() -> new NotificationException("Notification introuvable pour cet utilisateur"));

        NotificationSortieDTO sortie = notificationMappers.notificationToNotificationSortieDTO(notification);
        sortie.setNomCompletAuteur(notification.getAuteur().getNom() + " " + notification.getAuteur().getPrenom());
        sortie.setNomCompletUtilisateurRecoit(notification.getUtilisateur().getNom() + " " + notification.getUtilisateur().getPrenom());
        return sortie;
    }

    @Override
    public int marquerToutesNotificationsCommeLues(Long id) {
        return notificationRepository.marquerToutesCommeLuesParIdUtilisateur(id, LocalDateTime.now());
    }

    private Utilisateur requireAuteur(Long id) throws UtilisateursException {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateursException("Auteur introuvable"));
    }

    private void validate(NotificationEntrerDTO dto) {
        if (dto == null) {
            throw new NotificationException("Notification ne peut pas etre null");
        }
        if (dto.getTitre() == null || dto.getTitre().isBlank()) {
            throw new NotificationException("Titre obligatoire");
        }
        if (dto.getMessage() == null || dto.getMessage().isBlank()) {
            throw new NotificationException("Message obligatoire");
        }
        if (dto.getType() == null) {
            throw new NotificationException("Type obligatoire");
        }
        if (dto.getReferenceId() == null || dto.getReferenceType() == null) {
            throw new NotificationException("ReferenceId et ReferenceType obligatoires");
        }
        if (dto.getUtilisateurRecoitId() == null) {
            throw new NotificationException("UtilisateurRecoitId obligatoire");
        }
        if (dto.getAuteurId() == null) {
            throw new NotificationException("AuteurId obligatoire");
        }
    }

    private void validate(NotificationEntrerPourPlusieursUtilisateurDTO dto) {
        if (dto == null) {
            throw new NotificationException("Notification ne peut pas etre null");
        }
        if (dto.getTitre() == null || dto.getTitre().isBlank()) {
            throw new NotificationException("Titre obligatoire");
        }
        if (dto.getMessage() == null || dto.getMessage().isBlank()) {
            throw new NotificationException("Message obligatoire");
        }
        if (dto.getType() == null) {
            throw new NotificationException("Type obligatoire");
        }
        if (dto.getReferenceId() == null || dto.getReferenceType() == null) {
            throw new NotificationException("ReferenceId et ReferenceType obligatoires");
        }
        if (dto.getAuteurId() == null) {
            throw new NotificationException("AuteurId obligatoire");
        }
    }

    private void publierDiffusionsApresCommit(List<NotificationApresCommitEvenement.NotificationDiffusion> diffusions) {
        if (diffusions.isEmpty()) {
            return;
        }
        applicationEventPublisher.publishEvent(new NotificationApresCommitEvenement(diffusions));
    }



    private void diffuserPourDestinataires( NotificationEntrerPourPlusieursUtilisateurDTO dto, List<DestinataireNotification> destinataires) {
        if (destinataires.isEmpty()) {
            return;
        }

        Utilisateur auteur = requireAuteur(dto.getAuteurId());
        LocalDateTime maintenant = LocalDateTime.now();
        List<Notification> notifications = new ArrayList<>(destinataires.size());

        for (DestinataireNotification destinataire : destinataires) {
            Utilisateur utilisateurRef = entityManager.getReference(Utilisateur.class, destinataire.getId());
            notifications.add(Notification.builder()
                    .utilisateur(utilisateurRef)
                    .auteur(auteur)
                    .titre(dto.getTitre())
                    .message(dto.getMessage())
                    .type(dto.getType())
                    .referenceId(dto.getReferenceId())
                    .referenceType(dto.getReferenceType())
                    .dateEnvoi(maintenant)
                    .lu(false)
                    .build());
        }

        notificationRepository.saveAll(notifications);

        List<NotificationApresCommitEvenement.NotificationDiffusion> diffusions = new ArrayList<>(notifications.size());
        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            DestinataireNotification destinataire = destinataires.get(i);
            NotificationSortieDTO sortie = notificationMappers.notificationToNotificationSortieDTO(notification);
            sortie.setNomCompletAuteur(auteur.getNom() + " " + auteur.getPrenom());
            sortie.setNomCompletUtilisateurRecoit(destinataire.getNom() + " " + destinataire.getPrenom());
            diffusions.add(new NotificationApresCommitEvenement.NotificationDiffusion(destinataire.getEmail(), sortie));
        }

        publierDiffusionsApresCommit(diffusions);
    }


}
