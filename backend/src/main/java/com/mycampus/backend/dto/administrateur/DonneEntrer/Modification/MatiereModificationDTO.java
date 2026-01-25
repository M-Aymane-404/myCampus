package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class MatiereModificationDTO {
    
    private String nom;
    private String description;
    private Integer coefficient;
    private String codeModule;
}
