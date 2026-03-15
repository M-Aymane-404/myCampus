package com.mycampus.backend.dto.administrateur.DonneSortie;

import com.mycampus.backend.enumerations.EtatStructure;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class FiliereSortieDTO {

    private String codeFiliere;
    private  String nom;
    private  String description;
    private  Integer dureeFormation;
    private  String niveauDiplome;
    private  EtatStructure etat;
    private  String codeDepartement;
    private  String departementNom;
    private  String chefFilierePpr;
    private  String chefFiliereNom;
    private  String chefFilierePrenom;
    private  Integer nbModules;
    private  Integer nbEtudiants;


}
