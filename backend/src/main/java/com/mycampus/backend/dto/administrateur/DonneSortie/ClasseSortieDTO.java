package com.mycampus.backend.dto.administrateur.DonneSortie;

import lombok.*;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class ClasseSortieDTO {
    
    private  Long id;
    private  String codeClasse;
    private  String nom;
    private  String niveau;
    private  Integer effectifMax;
    private String emploiTemps;
    private  String codeFiliere;
    private  String filiereNom;
    private  String anneeUniversitaire;
}
