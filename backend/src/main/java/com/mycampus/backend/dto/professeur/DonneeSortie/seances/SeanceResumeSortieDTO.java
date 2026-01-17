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
public class SeanceResumeSortieDTO {

    private Long id;

    private LocalDate date;

    private LocalDateTime heureDebut;

    private LocalDateTime heureFin;

    private TypeSeance type;

    private String salle;

    private EtatSeance etat;
}
