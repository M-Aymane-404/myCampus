package com.mycampus.backend.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class StatistiquesGlobalesDTO {
    
    private final Long totalEtudiants;
    private final Long totalProfesseurs;
    private final Long totalAdministrateurs;
    private final Long totalUtilisateurs;
    private final Long totalUtilisateursActifs;
    private final Long totalDepartements;
    private final Long totalFilieres;
    private final Long totalClasses;
    private final Long totalModules;
    private final Long totalMatieres;
}
