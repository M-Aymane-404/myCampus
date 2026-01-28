package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

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

public class ClasseEntrerDTO {
    
    @NotBlank(message = "Code classe obligatoire")
    @Pattern(
            regexp = "^[A-Z0-9]{2,20}$",
            message = "Code classe doit contenir 2-20 caractères alphanumériques en majuscules"
    )
    private String codeClasse;
    
    @NotBlank(message = "Nom classe obligatoire")
    @Size(max = 100, message = "Nom classe ne doit pas dépasser 100 caractères")
    private String nom;
    
    @Size(max = 50, message = "Niveau ne doit pas dépasser 50 caractères")
    private String niveau;
    
    private Integer effectifMax;
    
    @Pattern(
            regexp = "^[A-Z0-9]{2,10}$",
            message = "Code filière doit contenir 2-10 caractères alphanumériques en majuscules"
    )
    private String codeFiliere;
    
    @Pattern(
            regexp = "^[0-9]{4}/[0-9]{4}$",
            message = "Année universitaire doit être au format AAAA/AAAA"
    )
    private String anneeUniversitaire;
    
    @Size(max = 500, message = "Emploi du temps ne doit pas dépasser 500 caractères")
    private String emploiTemps;
}
