package com.mycampus.backend.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class MatiereDTO {
    
    private  Long id;
    private  String codeMatiere;
    private  String nom;
    private  String description;
    private  Integer coefficient;
    private  Long moduleId;
    private  String moduleNom;
}
