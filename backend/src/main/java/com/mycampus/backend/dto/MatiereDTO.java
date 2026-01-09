package com.mycampus.backend.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class MatiereDTO {
    
    private final Long id;
    private final String codeMatiere;
    private final String nom;
    private final String description;
    private final Double coefficient;
    private final Long moduleId;
    private final String moduleNom;
}
