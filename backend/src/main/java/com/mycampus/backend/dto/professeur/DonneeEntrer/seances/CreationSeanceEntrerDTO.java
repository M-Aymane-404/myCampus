package com.mycampus.backend.dto.professeur.DonneeEntrer.seances;

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
public class CreationSeanceEntrerDTO {

    private Long affectationEnseignementId;

    private TypeSeance type;

    private LocalDate date;

    private LocalDateTime heureDebut;

    private LocalDateTime heureFin;

    private String salle;

    private String contenu;
}
