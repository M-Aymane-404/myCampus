package com.mycampus.backend.dto;

import lombok.*;
import com.mycampus.backend.enumerations.Semestre;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ModuleDTO {
    
    private  Long id;
    private  String codeModule;
    private  String nom;
    private  String description;
    private  Semestre semestre;
    private  Integer coefficient;
    private  Long filiereId;
    private  String filiereNom;
}
