package com.mycampus.backend.dto.administrateur.DonneSortie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class MatiereSortieAvecProfDTO {
    
    private  Long id;
    private  String codeMatiere;
    private  String nom;
    private  String description;
    private  Integer coefficient;
    private ProfesseurMiniSortieDTO professeur;


}
