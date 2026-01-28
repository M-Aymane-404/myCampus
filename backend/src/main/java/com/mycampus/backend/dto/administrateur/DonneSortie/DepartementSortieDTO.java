package com.mycampus.backend.dto.administrateur.DonneSortie;

import com.mycampus.backend.enumerations.EtatStructure;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class DepartementSortieDTO {

    private String codeDepartement;
    private String typeDepartement;
    private String description;
    private String chefDepartementPpr;
    private String chefDepartementNom;
    private EtatStructure etat;

    private Integer nbFilieres;
    private String chefDepartementPrenom;
    private LocalDate dateCreation;
    private Integer nbEtudiants;
}
