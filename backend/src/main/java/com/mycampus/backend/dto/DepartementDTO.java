package com.mycampus.backend.dto;

import com.mycampus.backend.entites.Professeur;
import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Builder
public class DepartementDTO {

    private final Long id;
    private final String codeDepartement;
    private final String typeDepartement;
    private final String description;
    private final Professeur chefDepartement;
    private final LocalDate dateCreation;
}
