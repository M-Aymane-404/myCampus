package com.mycampus.backend.dto;

import lombok.*;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class ClasseDTO {
    
    private  Long id;
    private  String codeClasse;
    private  String nom;
    private  String niveau;
    private  Integer effectifMax;
    private  Long filiereId;
    private  String filiereNom;
    private  String anneeUniversitaire;
}
