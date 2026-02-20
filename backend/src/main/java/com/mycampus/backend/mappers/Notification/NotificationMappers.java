package com.mycampus.backend.mappers.Notification;


import com.mycampus.backend.dto.Notification.NotificationEntrerDTO;
import com.mycampus.backend.dto.Notification.NotificationSortieDTO;
import com.mycampus.backend.entites.Notification;
import com.mycampus.backend.repositorys.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMappers {



    public NotificationSortieDTO notificationToNotificationSortieDTO(Notification notification) {
        NotificationSortieDTO notificationSortieDTO = new NotificationSortieDTO();
        BeanUtils.copyProperties(notification, notificationSortieDTO);
        return notificationSortieDTO;
    }

    public NotificationSortieDTO mapperProjectionInbox(NotificationRepository.NotificationInboxProjection projection) {

        return NotificationSortieDTO.builder()
                .id(projection.getId())
                .titre(projection.getTitre())
                .message(projection.getMessage())
                .dateEnvoi(projection.getDateEnvoi())
                .lu(projection.isLu())
                .dateLecture(projection.getDateLecture())
                .referenceId(projection.getReferenceId())
                .referenceType(projection.getReferenceType())
                .type(projection.getType())
                .nomCompletAuteur(projection.getNomAuteur() + " " + projection.getPrenomAuteur())
                .nomCompletUtilisateurRecoit(projection.getNomUtilisateur() + " " + projection.getPrenomUtilisateur())
                .build();
    }

 }
