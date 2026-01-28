package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import com.mycampus.backend.enumerations.EtatStructure;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "Code département obligatoire")
    private String codeDepartement;
    
    @NotBlank(message = "Type département obligatoire")
    private String typeDepartement;
    
    @NotBlank(message = "Description obligatoire")
    private String description;
    
    private String chefDepartementPpr;
    
    private LocalDate dateCreation;

    @NotNull(message = "État obligatoire")
    private EtatStructure etat;
}
