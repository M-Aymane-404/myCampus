package com.mycampus.backend.dto.professeur.DonneeSortie.presences;

import com.mycampus.backend.entites.Seance;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class FeuilleAppelPresenceSortieDTO {
    private Long seanceId;
    private LocalDate date;
    private LocalDateTime heureDebut;
    private LocalDateTime heureFin;
    private String salle;

    private String codeClasse;
    private String nomMatiere;

    private List<LignePresenceSortieDTO> lignes;

}
