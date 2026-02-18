package com.mycampus.backend.dto.Notification;

import com.mycampus.backend.enumerations.ReferenceType;
import com.mycampus.backend.enumerations.TypeNotification;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class NotificationEntrerPourPlusieursUtilisateurDTO {


    private String titre;

    private String message;

    private Long referenceId;


   private ReferenceType referenceType;


   private TypeNotification type;





   private Long auteurId;
}
