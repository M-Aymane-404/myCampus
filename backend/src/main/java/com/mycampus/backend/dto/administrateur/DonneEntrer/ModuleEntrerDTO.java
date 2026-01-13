package com.mycampus.backend.dto.administrateur.DonneEntrer;

import com.mycampus.backend.enumerations.Semestre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ModuleEntrerDTO {
    
    private  Long id;
    private  String codeModule;
    private  String nom;
    private  String description;
    private  Semestre semestre;
    private  Integer coefficient;
    private  Long filiereId;
}
