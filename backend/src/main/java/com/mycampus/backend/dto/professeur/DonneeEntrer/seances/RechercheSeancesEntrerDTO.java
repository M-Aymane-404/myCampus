package com.mycampus.backend.dto.professeur.DonneeEntrer.seances;



import com.mycampus.backend.enumerations.EtatSeance;
import com.mycampus.backend.enumerations.TypeSeance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechercheSeancesEntrerDTO {

    private Long affectationEnseignementId;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    private EtatSeance etat;

    private TypeSeance type;
}
