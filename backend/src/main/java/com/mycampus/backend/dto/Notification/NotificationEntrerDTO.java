package com.mycampus.backend.dto.Notification;

import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.enumerations.ReferenceType;
import com.mycampus.backend.enumerations.TypeNotification;
import jakarta.persistence.*;
import lombok.*;

 @NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class NotificationEntrerDTO {


     private String titre;

     private String message;

     private Long referenceId;


    private ReferenceType referenceType;


    private TypeNotification type;


    private Long  UtilisateurRecoitId;



    private Long auteurId;
}
