package com.mycampus.backend.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class StatistiquesParFiliereDTO {
    
    private final Long filiereId;
    private final String filiereNom;
    private final Long totalEtudiants;
    private final Long totalProfesseurs;
    private final Long totalModules;
}
