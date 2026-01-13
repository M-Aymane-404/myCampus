package com.mycampus.backend.dto.administrateur.DonneSortie;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

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



    private Long departementOuTravailId;


    private Long departementDirigeeId;


    private Long filiereDirigeeId;

}
