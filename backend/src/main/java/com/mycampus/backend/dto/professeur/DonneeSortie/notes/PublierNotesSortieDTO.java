package com.mycampus.backend.dto.professeur.DonneeSortie.notes;

import lombok.*;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
public class PublierNotesSortieDTO {
        private Long examenId;
        private int nbResultatsPublies;
    }

