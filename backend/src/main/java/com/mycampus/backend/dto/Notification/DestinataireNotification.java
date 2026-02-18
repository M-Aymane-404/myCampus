package com.mycampus.backend.dto.Notification;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DestinataireNotification {
    Long id;
    String nom;
    String prenom;
    String email;
}
