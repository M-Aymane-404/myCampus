package com.mycampus.backend.metier.Notification;

import com.mycampus.backend.dto.Notification.NotificationSortieDTO;

public interface NotificationWebSocketMetier {


    void notifierUtilisateur(String email, NotificationSortieDTO notificationSortieDTO);
}
