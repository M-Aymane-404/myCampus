package com.mycampus.backend.dto.professeur.DonneeSortie.presences;

import com.mycampus.backend.enumerations.StatusAbsence;
import lombok.*;

import java.time.LocalDateTime;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter


public class LignePresenceSortieDTO {
    private Long idPresence;      // null si pas encore enregistré en base
    private Long idEtudiant;


    private String apogee;
private String nomEtudiant;
    private String prenomEtudiant;

    private StatusAbsence status;
    private boolean justifiee;
    private String motif;
    private LocalDateTime dateDeclaration;
}
