package com.mycampus.backend.dto.etudiant.DonneeSortie;

import com.mycampus.backend.enumerations.TypeSeance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MiniSeanceSortieEtudiantDTO {

    private Long id;
    private LocalDate date;
    private String salle;
    private TypeSeance type;
    private String prof;
    private String codeMatiere;
    private String matiereNom;
}
