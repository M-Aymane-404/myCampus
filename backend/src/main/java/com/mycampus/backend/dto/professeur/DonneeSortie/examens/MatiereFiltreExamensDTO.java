package com.mycampus.backend.dto.professeur.DonneeSortie.examens;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatiereFiltreExamensDTO {
    private String codeMatiere;
    private String nomMatiere;
}