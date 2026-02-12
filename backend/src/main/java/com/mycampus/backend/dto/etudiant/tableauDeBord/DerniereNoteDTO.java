package com.mycampus.backend.dto.etudiant.tableauDeBord;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DerniereNoteDTO {
    
    private String matiere;
    private Double note;
    private String datePublication;

}
