package com.mycampus.backend.dto.etudiant.DonneeSortie;

import com.mycampus.backend.enumerations.StatusAbsence;
import lombok.*;

import java.time.LocalDateTime;

 @Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PresenceSortieEtudiantDTO {
    
      private Long id;

    private StatusAbsence status;

    private Boolean justifiee ;
    
    private String motif;

    private LocalDateTime dateDeclaration;

    private MiniSeanceSortieEtudiantDTO seance;

}
