package com.mycampus.backend.dto.annonces;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuteurDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
}
