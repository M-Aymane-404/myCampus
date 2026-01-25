package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

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

    private String codeFiliere;
    private  String nom;
    private  String description;
    private  String dureeFormation;
    private  String niveauDiplome;
    private  EtatStructure etat;
    private  String codeDepartement;
    private  String chefFilierePpr;

}
