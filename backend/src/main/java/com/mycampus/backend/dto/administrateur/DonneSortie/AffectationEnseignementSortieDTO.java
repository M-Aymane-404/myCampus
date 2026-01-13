package com.mycampus.backend.dto.administrateur.DonneSortie;

import com.mycampus.backend.enumerations.Semestre;
import lombok.*;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AffectationEnseignementSortieDTO {
    
    private Long id;
    private String anneeUniversitaire;
    private Semestre semestre;

    private Boolean active;
    private String professeurPpr;
    private String professeurNom;
    private Long classeId;
    private String classeNom;
    private Long matiereId;
    private String matiereNom;
}
