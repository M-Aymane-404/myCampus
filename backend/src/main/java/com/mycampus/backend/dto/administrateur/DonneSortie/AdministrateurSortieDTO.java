package com.mycampus.backend.dto.administrateur.DonneSortie;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data

public class AdministrateurSortieDTO extends UtilisateurSortieDto {

    private String ppr;

    private String grade;

    private String fonction;
}
