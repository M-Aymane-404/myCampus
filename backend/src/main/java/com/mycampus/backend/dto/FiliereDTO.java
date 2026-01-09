package com.mycampus.backend.dto;

import com.mycampus.backend.enumerations.EtatStructure;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class FiliereDTO {
    
    private final Long id;
    private final String codeFiliere;
    private final String nom;
    private final String description;
    private final Integer dureeFormation;
    private final String niveauDiplome;
    private final EtatStructure etat;
    private final Long departementId;
    private final String departementNom;
}
