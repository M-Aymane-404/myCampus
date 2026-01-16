package com.mycampus.backend.dto.administrateur.DonneEntrer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

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
