package com.mycampus.backend.dto.professeur.DonneeSortie.presences;

import com.mycampus.backend.enumerations.StatusAbsence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class LignePresenceSortieDTO {
    private Long id;


    private String apogee;
private String nomEtudiant;
    private String prenomEtudiant;

    private StatusAbsence status;
    private boolean justifiee;
    private String motif;
    private LocalDateTime dateDeclaration;
}
