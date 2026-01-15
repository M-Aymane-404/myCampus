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
    private String codeClasse;
    private String classeNom;
    private String codeMatiere;
    private String matiereNom;
}
