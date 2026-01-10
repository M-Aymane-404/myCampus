package com.mycampus.backend.dto;

import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.enumerations.EtatStructure;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class FiliereDTO {

    private  Long id;
    private  String codeFiliere;
    private  String nom;
    private  String description;
    private  String dureeFormation;
    private  String niveauDiplome;
    private  EtatStructure etat;
    private  Long departementId;
    private  String departementNom;
    private  Long chef_filiere_id;

}
