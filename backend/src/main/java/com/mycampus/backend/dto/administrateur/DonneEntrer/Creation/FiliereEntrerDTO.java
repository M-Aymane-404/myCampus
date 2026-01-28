package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import com.mycampus.backend.enumerations.EtatStructure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String codeFiliere;
    
    @NotBlank(message = "Nom filière obligatoire")
    private String nom;
    
    @NotBlank(message = "Description obligatoire")
    private String description;
    
    @NotBlank(message = "Durée de formation obligatoire")
    private String dureeFormation;
    
    @NotBlank(message = "Niveau diplôme obligatoire")
    private String niveauDiplome;
    
    @NotNull(message = "État obligatoire")
    private EtatStructure etat;
    
    @NotBlank(message = "Code département obligatoire")
    private String codeDepartement;
    
    private String chefFilierePpr;

}
