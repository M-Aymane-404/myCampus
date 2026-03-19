package com.mycampus.backend.dto.administrateur.DonneSortie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data

public class UtilisateurSortieDto {
    private Long id;

    private String nom;

    private String prenom;

    private String email;

    private String cin;

    private String tel;

    private String photoUrl;
    private String photoNom;


    private Boolean active ;

    private LocalDateTime dateCreation;

    private LocalDateTime dateDerniereConnexion;


}
