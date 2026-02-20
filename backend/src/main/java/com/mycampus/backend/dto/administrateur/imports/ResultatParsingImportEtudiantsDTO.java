package com.mycampus.backend.dto.administrateur.imports;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResultatParsingImportEtudiantsDTO {
    
    private List<EtudiantImportDTO> etudiants;
    private List<ErreurImportDTO> erreurs;
    private int totalLignesLues;
}
