package com.mycampus.backend.dto.professeur.DonneeEntrer.notes;

import lombok.*;


 @Getter
 @Setter
 @NoArgsConstructor
 @AllArgsConstructor
 @Builder
public class SaisieNoteEntrerDTO {
        private Long idEtudiant;
        private Double note;

}
