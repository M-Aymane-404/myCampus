package com.mycampus.backend.dto.administrateur.DonneEntrer;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
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



    private String codeDepartementOuTravail;





}
