package com.mycampus.backend.dto.administrateur.DonneEntrer;

import com.mycampus.backend.entites.Professeur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class DepartementEntrerDTO {

    private  Long id;
    private  String codeDepartement;
    private  String typeDepartement;
    private  String description;
    private  Professeur chefDepartement;
    private  LocalDate dateCreation;
}
