package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import com.mycampus.backend.enumerations.EtatStructure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Pattern(
            regexp = "^[A-Z0-9]{2,10}$",
            message = "Code département doit contenir 2-10 caractères alphanumériques en majuscules"
    )
    private String codeDepartement;
    
    @NotBlank(message = "Type département obligatoire")
    @Size(max = 100, message = "Type département ne doit pas dépasser 100 caractères")
    private String typeDepartement;
    
    @Size(max = 500, message = "Description ne doit pas dépasser 500 caractères")
    private String description;
    
    @Pattern(
            regexp = "^[0-9]{8}$",
            message = "PPR du chef de département doit contenir 8 chiffres"
    )
    private String chefDepartementPpr;
    
    private LocalDate dateCreation;

    private EtatStructure etat;
}
