package com.mycampus.backend.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class StatistiquesParDepartementDTO {
    
    private final Long departementId;
    private final String departementNom;
    private final Long totalFilieres;
    private final Long totalProfesseurs;
    private final Long totalEtudiants;
}
