package com.mycampus.backend.dto.professeur.DonneeEntrer.examens;

import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.TypeExamen;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamenCreationEntrerDTO {

        private Semestre semestre;
        private Date dateExamen;
        private Integer numeroExamen;
        private TypeExamen typeExamen; // NORMAL / RATTRAPAGE
        private Integer duree;         // minutes
    }

