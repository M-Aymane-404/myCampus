package com.mycampus.backend.dto.administrateur.DonneSortie;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProfesseurSortieDTO extends UtilisateurSortieDto {
    private String ppr;

    private String specialite;

    private String grade;

    private LocalDate dateRecrutement;

    private String statut;



    private String codeDepartementOuTravail;


    private String codeDepartementDirigee;


    private String codeFiliereDirigee;

    // Liste des matières enseignées par le professeur (noms)
    private List<String> matieresEnseignees;

    // Liste des filières où le professeur enseigne (codes)
    private List<String> filieres;

}
