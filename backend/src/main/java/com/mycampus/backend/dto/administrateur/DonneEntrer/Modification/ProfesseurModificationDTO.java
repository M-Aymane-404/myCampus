package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;


import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.UtilisateurEntrerDto;
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
public class ProfesseurModificationDTO extends UtilisateurModificationDto {
    private String ppr;

    private String specialite;

    private String grade;

    private LocalDate dateRecrutement;

    private String statut;



    private String codeDepartementOuTravail;





}
