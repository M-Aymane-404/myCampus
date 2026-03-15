package com.mycampus.backend.dto.professeur.DonneeEntrer.examens;
import com.mycampus.backend.enumerations.EtatExamen;
import lombok.*;

import java.util.Date;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder

public class ExamenModificationEntrerDTO {

        private Date dateExamen;
        private Integer duree;
        private EtatExamen etat;
}
