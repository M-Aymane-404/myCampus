package com.mycampus.backend.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;
import java.util.Date;

@Getter
@AllArgsConstructor
public class AdministrateurDTO {
    
    private final Long id;
    private final String ppr;
    private final String nom;
    private final String prenom;
    private final String email;
    private final String fonction;
    private final String grade;
    private final boolean enabled;
    private final Date dateCreation;
    private final Date dateDerniereConnexion;
}
