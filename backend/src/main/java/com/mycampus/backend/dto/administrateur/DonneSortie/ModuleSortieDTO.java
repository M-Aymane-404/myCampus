package com.mycampus.backend.dto.administrateur.DonneSortie;

import lombok.*;
import com.mycampus.backend.enumerations.Semestre;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ModuleSortieDTO {
    
    private  Long id;
    private  String codeModule;
    private  String nom;
    private  String description;
    private  Semestre semestre;
      private  String codeFiliere;
    private  String filiereNom;
    private Integer nbMatieres;
}
