package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class DepartementModificationDTO {


    private String typeDepartement;
    private String description;
    private String chefDepartementPpr;
    private LocalDate dateCreation;
}
