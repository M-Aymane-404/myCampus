package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class ClasseEntrerDTO {
    
    @NotBlank(message = "Code classe obligatoire")
    private String codeClasse;
    
    @NotBlank(message = "Nom classe obligatoire")
    private String nom;
    
    @NotBlank(message = "Niveau obligatoire")
    private String niveau;
    
    @Min(value = 1, message = "Effectif minimum est 1")
    @Max(value = 200, message = "Effectif maximum est 200")
    private Integer effectifMax;
    
    @NotBlank(message = "Code filière obligatoire")
    private String codeFiliere;
    
    @NotBlank(message = "Année universitaire obligatoire")
    private String anneeUniversitaire;
    
    private String emploiTemps;

}
