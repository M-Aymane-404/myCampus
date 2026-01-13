package com.mycampus.backend.dto.administrateur.DonneEntrer;

import com.mycampus.backend.enumerations.Semestre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AffectationEnseignementEntrerDTO {
    
    private Long id;
    private String anneeUniversitaire;
    private Semestre semestre;

    private Boolean active;
    private String professeurPpr;
    private Long classeId;
    private Long matiereId;
}
