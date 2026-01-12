package com.mycampus.backend.dto;


import com.mycampus.backend.entites.Utilisateur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProfesseurDTO extends UtilisateurDto {
    private String ppr;

    private String specialite;

    private String grade;

    private LocalDate dateRecrutement;

    private String statut;



    private Long departementOuTravailId;


    private Long departementDirigeeId;


    private Long filiereDirigeeId;

}
