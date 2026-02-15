package com.mycampus.backend.metier.Notification;

import com.mycampus.backend.dto.Notification.NotificationEntrerDTO;
import com.mycampus.backend.dto.Notification.NotificationEntrerPourPlusieursUtilisateurDTO;
import com.mycampus.backend.dto.Notification.NotificationSortieDTO;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.enumerations.TypeNotification;
import com.mycampus.backend.exceptions.NotificationException;

public interface NotificationMetier {

    NotificationSortieDTO notifierUtilisateur(NotificationEntrerDTO notificationEntrerDTO) throws NotificationException;

    void notifierEtudiantClasse(String codeClasse, NotificationEntrerPourPlusieursUtilisateurDTO notificationEntrerPourPlusieursUtilisateurDTO) throws NotificationException;

    void notifierProfesseurDepartement(String codeDepartement, NotificationEntrerPourPlusieursUtilisateurDTO notificationEntrerPourPlusieursUtilisateurDTO) throws NotificationException;
}
