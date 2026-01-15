package com.mycampus.backend.dto.administrateur.DonneSortie;

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
    private String chefDepartementPrenom;
    private LocalDate dateCreation;
}
