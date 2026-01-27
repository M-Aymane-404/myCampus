package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;

import com.mycampus.backend.enumerations.Semestre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AffectationEnseignementModificationDTO {
    

    private Boolean active;
    private String professeurPpr;
    private String codeClasse;
    private String codeMatiere;
}
