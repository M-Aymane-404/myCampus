package com.mycampus.backend.dto.etudiant.DonneeSortie;

import com.mycampus.backend.enumerations.StatutJustificatif;
import com.mycampus.backend.enumerations.StatusAbsence;
import com.mycampus.backend.enumerations.TypeMotif;
import lombok.*;

import java.time.LocalDateTime;

 @Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PresenceSortieEtudiantDTO {
    
      private Long id;

    private StatusAbsence status;
     private String descriptionDeJustification;

     private StatutJustificatif statutJustificatif;
     private boolean justificationSoumise;
     private boolean justificationValidee;

      private TypeMotif typeMotif;

     private String motifUrl;

     private String motifNom;

    private LocalDateTime dateDeclaration;

    private MiniSeanceSortieEtudiantDTO seance;

}
