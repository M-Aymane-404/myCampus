package com.mycampus.backend.dto.administrateur.DonneSortie;

import com.mycampus.backend.enumerations.EtatStructure;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class FiliereSortieDTO {

    private  Long id;
    private  String codeFiliere;
    private  String nom;
    private  String description;
    private String emploiTemps;
    private  String dureeFormation;
    private  String niveauDiplome;
    private  EtatStructure etat;
    private  Long departementId;
    private  String departementNom;
    private  Long chef_filiere_id;

}
