package com.mycampus.backend.dto.etudiant.DonneeSortie;

import com.mycampus.backend.enumerations.Semestre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ModuleSortieEtudiantDTO {
    
    private  Long id;
    private  String codeModule;
    private  String nom;
    private  String description;
    private  Semestre semestre;

}
