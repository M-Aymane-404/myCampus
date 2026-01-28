package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
    private String codeMatiere;
    
    @NotBlank(message = "Nom matière obligatoire")
    private String nom;
    
    @NotBlank(message = "Description matière obligatoire")
    private String description;
    
    @Min(value = 1, message = "Coefficient minimum est 1")
    private Integer coefficient;
    
    @NotBlank(message = "Code module obligatoire")
    private String codeModule;
}
