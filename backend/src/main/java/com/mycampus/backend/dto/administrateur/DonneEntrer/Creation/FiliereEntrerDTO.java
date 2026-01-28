package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import com.mycampus.backend.enumerations.EtatStructure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class FiliereEntrerDTO {

    @NotBlank(message = "Code filière obligatoire")
    @Pattern(
            regexp = "^[A-Z0-9]{2,10}$",
            message = "Code filière doit contenir 2-10 caractères alphanumériques en majuscules"
    )
    private String codeFiliere;
    
    @NotBlank(message = "Nom filière obligatoire")
    @Size(max = 100, message = "Nom filière ne doit pas dépasser 100 caractères")
    private String nom;
    
    @Size(max = 500, message = "Description ne doit pas dépasser 500 caractères")
    private String description;
    
    @Size(max = 50, message = "Durée formation ne doit pas dépasser 50 caractères")
    private String dureeFormation;
    
    @Size(max = 50, message = "Niveau diplôme ne doit pas dépasser 50 caractères")
    private String niveauDiplome;
    
    private EtatStructure etat;
    
    @Pattern(
            regexp = "^[A-Z0-9]{2,10}$",
            message = "Code département doit contenir 2-10 caractères alphanumériques en majuscules"
    )
    private String codeDepartement;
    
    @Pattern(
            regexp = "^[0-9]{8}$",
            message = "PPR du chef de filière doit contenir 8 chiffres"
    )
    private String chefFilierePpr;
}
