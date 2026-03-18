package com.mycampus.backend.dto.professeur.DonneeSortie.presences;

import com.mycampus.backend.enumerations.StatutJustificatif;
import com.mycampus.backend.enumerations.StatusAbsence;
import com.mycampus.backend.enumerations.TypeMotif;
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
    private StatutJustificatif statutJustificatif;
    private boolean justifiee;
    private boolean justificationSoumise;
    private String motif;
    private TypeMotif typeMotif;
    private String motifUrl;
    private String motifNom;
    private LocalDateTime dateDeclaration;
}
