package com.mycampus.backend.dto.securite.sortie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurMiniDTO {

    private Long id;

    private String nom;

    private String prenom;

    private String email;

    private String typeUtilisateur;
}
