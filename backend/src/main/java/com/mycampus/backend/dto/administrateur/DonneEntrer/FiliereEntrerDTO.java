package com.mycampus.backend.dto.administrateur.DonneEntrer;

import com.mycampus.backend.enumerations.EtatStructure;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class FiliereEntrerDTO {

    private  Long id;
    private  String codeFiliere;
    private  String nom;
    private  String description;
    private String emploiTemps;
    private  String dureeFormation;
    private  String niveauDiplome;
    private  EtatStructure etat;
    private  Long departementId;
    private  Long chef_filiere_id;

}
