package com.mycampus.backend.dto.etudiant.tableauDeBord;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableauDeBordEtudiantSortieDTO {
    
    private EnteteEtudiantDTO entete;
    private List<DerniereNoteDTO> dernieresNotes;
    private AssiduiteDTO assiduite;
    

    

    

    

}
