package com.mycampus.backend.dto.administrateur.DonneSortie;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class MatiereSortieDTO {
    
    private  Long id;
    private  String codeMatiere;
    private  String nom;
    private  String description;
    private  Integer coefficient;
    private  String codeModule;
    private  String moduleNom;
    private ProfesseurMiniSortieDTO professeur;

}
