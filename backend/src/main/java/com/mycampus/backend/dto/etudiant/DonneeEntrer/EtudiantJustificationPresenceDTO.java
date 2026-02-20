package com.mycampus.backend.dto.etudiant.DonneeEntrer;

import com.mycampus.backend.enumerations.TypeMotif;

 import lombok.*;


@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EtudiantJustificationPresenceDTO {

    private String descriptionDeJustification;

     private TypeMotif typeMotif;

}
