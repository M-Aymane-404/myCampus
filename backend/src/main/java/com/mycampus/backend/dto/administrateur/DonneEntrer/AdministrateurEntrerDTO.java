package com.mycampus.backend.dto.administrateur.DonneEntrer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data

public class AdministrateurEntrerDTO extends UtilisateurEntrerDto {

    private String ppr;

    private String grade;

    private String fonction;
}
