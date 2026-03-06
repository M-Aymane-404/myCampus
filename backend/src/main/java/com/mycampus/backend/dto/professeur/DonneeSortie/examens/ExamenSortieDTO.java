package com.mycampus.backend.dto.professeur.DonneeSortie.examens;


import com.mycampus.backend.enumerations.EtatExamen;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.TypeExamen;
import lombok.*;

import java.util.Date;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder

public class ExamenSortieDTO {
    private Long idExamen;

    private String codeMatiere;
    private String nomMatiere;

    private Semestre semestre;
    private Integer numeroExamen;
    private TypeExamen typeExamen;

    private Date dateExamen;
    private Integer duree;

    private EtatExamen etat;

    private boolean corrigeDisponible;
}
