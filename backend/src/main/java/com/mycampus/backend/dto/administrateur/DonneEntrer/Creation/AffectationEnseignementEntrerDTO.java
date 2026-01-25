package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

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
    
    private String anneeUniversitaire;
    private Semestre semestre;
    private Boolean active;
    private String professeurPpr;
    private String codeClasse;
    private String codeMatiere;
}
