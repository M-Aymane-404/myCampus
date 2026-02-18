package com.mycampus.backend.dto.administrateur.imports;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErreurImportDTO {
    
    private int ligne;
    private String champ;
    private String message;
    private String valeur;
}
