package com.mycampus.backend.dto.etudiant.tableauDeBord;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssiduiteDTO {
    
    private Double tauxPresence;
    private String labelQualite;
}
