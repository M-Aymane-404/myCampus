package com.mycampus.backend.dto.administrateur.DonneEntrer;

import com.mycampus.backend.enumerations.Semestre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModuleEntrerDTO {
    
    private String codeModule;
    private String nom;
    private String description;
    private Semestre semestre;
    private Integer coefficient;
    private String codeFiliere;
}
