package com.mycampus.backend.dto.professeur.DonneeSortie.notes;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class StatsNotesSortieDTO {
    private Double moyenne;
    private Double min;
    private Double max;

    private Integer totalEtudiants;
    private Integer nbNotesSaisies;
}
