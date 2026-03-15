package com.mycampus.backend.dto.administrateur.DonneSortie.TableauBordDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertesTableauBordDTO {
    private Long matieresSansProfesseur;
    private Long professeursSansAffectation;
}
