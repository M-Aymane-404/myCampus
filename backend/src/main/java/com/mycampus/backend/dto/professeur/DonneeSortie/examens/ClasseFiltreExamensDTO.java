package com.mycampus.backend.dto.professeur.DonneeSortie.examens;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClasseFiltreExamensDTO {

        private String codeClasse;
        private String nomClasse;
        private List<MatiereFiltreExamensDTO> matieres;
    }

