package com.mycampus.backend.metier.Notification;

import com.mycampus.backend.dto.Notification.NotificationEntrerDTO;
import com.mycampus.backend.dto.Notification.NotificationEntrerPourPlusieursUtilisateurDTO;
import com.mycampus.backend.dto.Notification.NotificationSortieDTO;
import com.mycampus.backend.exceptions.NotificationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationMetier {

    NotificationSortieDTO notifierUtilisateur(NotificationEntrerDTO notificationEntrerDTO) throws NotificationException;

    void notifierEtudiantClasse(String codeClasse, NotificationEntrerPourPlusieursUtilisateurDTO notificationEntrerPourPlusieursUtilisateurDTO) throws NotificationException;

    void notifierProfesseurDepartement(String codeDepartement, NotificationEntrerPourPlusieursUtilisateurDTO notificationEntrerPourPlusieursUtilisateurDTO) throws NotificationException;

    void notifierToutsProfesseur(NotificationEntrerPourPlusieursUtilisateurDTO notificationEntrerPourPlusieursUtilisateurDTO) throws NotificationException;

    void notifierToutsEtudiant(NotificationEntrerPourPlusieursUtilisateurDTO dto) throws NotificationException;

    Page<NotificationSortieDTO> listerNotificationsUtilisateur(String email, Pageable pageable);

    long compterNotificationsNonLuesUtilisateur(String email);

    NotificationSortieDTO marquerNotificationCommeLue(String email, Long notificationId);

    int marquerToutesNotificationsCommeLues(String email);
}
