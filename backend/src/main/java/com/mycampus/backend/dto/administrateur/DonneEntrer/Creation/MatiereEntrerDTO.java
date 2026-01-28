package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class MatiereEntrerDTO {
    
    @NotBlank(message = "Code matière obligatoire")
    @Pattern(
            regexp = "^[A-Z0-9]{2,20}$",
            message = "Code matière doit contenir 2-20 caractères alphanumériques en majuscules"
    )
    private String codeMatiere;
    
    @NotBlank(message = "Nom matière obligatoire")
    @Size(max = 100, message = "Nom matière ne doit pas dépasser 100 caractères")
    private String nom;
    
    @Size(max = 500, message = "Description ne doit pas dépasser 500 caractères")
    private String description;
    
    private Integer coefficient;
    
    @Pattern(
            regexp = "^[A-Z0-9]{2,20}$",
            message = "Code module doit contenir 2-20 caractères alphanumériques en majuscules"
    )
    private String codeModule;
}
