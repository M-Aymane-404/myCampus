package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import com.mycampus.backend.enumerations.EtatStructure;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    private String codeDepartement;
    private String typeDepartement;
    private String description;
    private String chefDepartementPpr;
    private LocalDate dateCreation;

    private EtatStructure etat;
}
