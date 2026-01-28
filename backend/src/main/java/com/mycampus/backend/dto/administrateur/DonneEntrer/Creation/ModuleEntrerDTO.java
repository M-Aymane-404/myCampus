package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import com.mycampus.backend.enumerations.Semestre;
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
public class ModuleEntrerDTO {
    
    @NotBlank(message = "Code module obligatoire")
    private String codeModule;
    
    @NotBlank(message = "Nom module obligatoire")
    private String nom;
    
    @NotBlank(message = "Description module obligatoire")
    private String description;
    
    private Semestre semestre;
    
    @Min(value = 1, message = "Coefficient minimum est 1")
    private Integer coefficient;
    
    @NotBlank(message = "Code filière obligatoire")
    private String codeFiliere;
}
