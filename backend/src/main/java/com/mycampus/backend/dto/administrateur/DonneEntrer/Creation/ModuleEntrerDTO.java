package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import com.mycampus.backend.enumerations.Semestre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ModuleEntrerDTO {
    
    @NotBlank(message = "Code module obligatoire")
    @Pattern(
            regexp = "^[A-Z0-9]{2,20}$",
            message = "Code module doit contenir 2-20 caractères alphanumériques en majuscules"
    )
    private String codeModule;
    
    @NotBlank(message = "Nom module obligatoire")
    @Size(max = 100, message = "Nom module ne doit pas dépasser 100 caractères")
    private String nom;
    
    @Size(max = 500, message = "Description ne doit pas dépasser 500 caractères")
    private String description;
    
    @NotNull(message = "Semestre obligatoire")
    private Semestre semestre;
    

    @Pattern(
            regexp = "^[A-Z0-9]{2,10}$",
            message = "Code filière doit contenir 2-10 caractères alphanumériques en majuscules"
    )
    private String codeFiliere;
}
