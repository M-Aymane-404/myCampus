package com.mycampus.backend.dto.professeur.DonneeSortie.seances;

import com.mycampus.backend.enumerations.EtatSeance;
import com.mycampus.backend.enumerations.TypeSeance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeanceSortieDTO {

    private Long id;

    private Long affectationEnseignementId;

    private TypeSeance type;

    private LocalDate date;

    private LocalDateTime heureDebut;

    private LocalDateTime heureFin;

    private String salle;

    private EtatSeance etat;

    private String contenu;

    private Long classeId;

    private Long matiereId;
}
