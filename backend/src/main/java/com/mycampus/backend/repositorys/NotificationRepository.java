package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Notification;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.enumerations.TypeNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    boolean existsByUtilisateurAndTypeAndDateEnvoi(Utilisateur utilisateur, TypeNotification type, LocalDateTime dateEnvoi);
}
