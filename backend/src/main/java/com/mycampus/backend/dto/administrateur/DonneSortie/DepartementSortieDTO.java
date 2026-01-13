package com.mycampus.backend.dto.administrateur.DonneSortie;

import com.mycampus.backend.entites.Professeur;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class DepartementSortieDTO {

    private  Long id;
    private  String codeDepartement;
    private  String typeDepartement;
    private  String description;
    private  Professeur chefDepartement;
    private  LocalDate dateCreation;
}
