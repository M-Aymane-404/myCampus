package com.mycampus.backend.dto.annonces;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuteurEtudiantDTO {
    private Long id;
    private String nom;
    private String prenom;
 }
