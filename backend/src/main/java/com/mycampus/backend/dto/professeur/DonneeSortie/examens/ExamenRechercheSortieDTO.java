package com.mycampus.backend.dto.professeur.DonneeSortie.examens;


import com.mycampus.backend.enumerations.EtatExamen;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.TypeExamen;
import lombok.*;

import java.util.Date;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExamenRechercheSortieDTO {

        private Long idExamen;

        private String codeClasse;
        private String nomClasse;

        private String codeMatiere;
        private String nomMatiere;

        private Semestre semestre;
        private Integer numeroExamen;
        private TypeExamen typeExamen;
        private EtatExamen etat;

        private Date dateExamen;
        private Integer duree;

        private boolean corrigeDisponible;
    }
