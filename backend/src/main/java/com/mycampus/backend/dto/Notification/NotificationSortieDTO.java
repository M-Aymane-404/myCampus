package com.mycampus.backend.dto.Notification;


import com.mycampus.backend.enumerations.ReferenceType;
import com.mycampus.backend.enumerations.TypeNotification;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSortieDTO {

    private Long id;

     private String titre;

     private String message;

     private LocalDateTime dateEnvoi;


    private boolean lu ;


    private LocalDateTime dateLecture;


    private Long referenceId;


    private ReferenceType referenceType;


    private TypeNotification type;


    private String nomCompletUtilisateurRecoit;



    private String nomCompletAuteur;
}
