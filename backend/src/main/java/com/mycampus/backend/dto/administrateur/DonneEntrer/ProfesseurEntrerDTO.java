package com.mycampus.backend.dto.administrateur.DonneEntrer;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProfesseurEntrerDTO extends UtilisateurEntrerDto {
    private String ppr;

    private String specialite;

    private String grade;

    private LocalDate dateRecrutement;

    private String statut;



    private Long departementOuTravailId;


    private Long departementDirigeeId;


    private Long filiereDirigeeId;

}
