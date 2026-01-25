package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data

public class UtilisateurEntrerDto {

    private String nom;

    private String prenom;

    private String email;

    private String password;

    private String cin;

    private String tel;

    private String photo;

    private Boolean enabled ;



}
