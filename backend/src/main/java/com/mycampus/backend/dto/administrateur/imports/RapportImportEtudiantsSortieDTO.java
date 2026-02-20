package com.mycampus.backend.dto.administrateur.imports;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RapportImportEtudiantsSortieDTO {
    
    private int totalLignes;
    private int nombreInseres;
    private int nombreIgnores;
    private int nombreErreurs;
    private boolean motDePasseGenere;
    private List<ErreurImportDTO> erreurs;
}
