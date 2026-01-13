package com.mycampus.backend.dto;

import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.TypeSeance;
import lombok.*;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AffectationEnseignementDTO {
    
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
