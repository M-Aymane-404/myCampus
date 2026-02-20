package com.mycampus.backend.metier.Notification;

import com.mycampus.backend.dto.Notification.NotificationSortieDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationWebSocketMetierImpl implements NotificationWebSocketMetier {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void notifierUtilisateur(String email, NotificationSortieDTO notificationSortieDTO) {
        messagingTemplate.convertAndSendToUser(
                email,
                "/queue/notifications",
                notificationSortieDTO
        );
    }



}
