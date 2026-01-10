package com.mycampus.backend.dto;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ModuleDTO {
    
    private  Long id;
    private  String codeModule;
    private  String nom;
    private  String description;
    private  String semestre;
    private  Integer coefficient;
    private  Long filiereId;
    private  String filiereNom;
}
