package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;

import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.UtilisateurEntrerDto;
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

public class AdministrateurModificationDTO extends UtilisateurModificationDto {

    private String ppr;

    private String grade;

    private String fonction;
}
