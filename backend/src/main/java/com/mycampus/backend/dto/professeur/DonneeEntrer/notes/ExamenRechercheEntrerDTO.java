package com.mycampus.backend.dto.professeur.DonneeEntrer.notes;

import com.mycampus.backend.enumerations.EtatExamen;
import com.mycampus.backend.enumerations.TypeExamen;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamenRechercheEntrerDTO {

        private String codeClasse;
        private String codeMatiere;
        private String anneeUniversitaire;
        private TypeExamen typeExamen;
        private EtatExamen etat;
        private Integer numeroExamen;

}
