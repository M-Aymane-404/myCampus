package com.mycampus.backend.dto.administrateur.DonneSortie.TableauBordDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatistiquesGlobalesDTO {
    private Long totalEtudiants;
    private Long totalProfesseurs;
    private Long totalClasses;
    private Long totalMatieres;
    private Long totalAffectationsActives;
}
