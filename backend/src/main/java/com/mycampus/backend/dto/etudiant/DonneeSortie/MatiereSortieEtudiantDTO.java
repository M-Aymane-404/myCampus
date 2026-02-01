package com.mycampus.backend.dto.etudiant.DonneeSortie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class MatiereSortieEtudiantDTO {
    
    private  Long id;
    private  String codeMatiere;
    private  String nom;
    private  String description;
     private String professeurDeMatiere ;


}
