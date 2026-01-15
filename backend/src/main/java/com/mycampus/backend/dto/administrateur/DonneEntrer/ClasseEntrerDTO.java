package com.mycampus.backend.dto.administrateur.DonneEntrer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class ClasseEntrerDTO {
    
    private String codeClasse;
    private String nom;
    private String niveau;
    private Integer effectifMax;
    private String codeFiliere;
    private String anneeUniversitaire;
}
