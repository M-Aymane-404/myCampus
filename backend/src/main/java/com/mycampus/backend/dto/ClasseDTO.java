package com.mycampus.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
public class ClasseDTO {
    
    private final Long id;
    private final String codeClasse;
    private final String nom;
    private final String niveau;
    private final Integer effectifMax;
    private final Long filiereId;
    private final String filiereNom;
}
