package com.mycampus.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
public class ModuleDTO {
    
    private final Long id;
    private final String codeModule;
    private final String nom;
    private final String description;
    private final String semestre;
    private final Integer coefficient;
    private final Long classeId;
    private final String classeNom;
}
