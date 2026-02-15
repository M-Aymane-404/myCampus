package com.mycampus.backend.dto.professeur.DonneeEntrer.seances;

import com.mycampus.backend.enumerations.EtatSeance;
import com.mycampus.backend.enumerations.TypeSeance;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Code classe obligatoire")
    private String codeClasse;

    @NotBlank(message = "Code matière obligatoire")
    private String codeMatiere;
//++
    @NotBlank(message = "Année universitaire obligatoire")
    private String anneeUniversitaire;


    private TypeSeance type;

    private LocalDate date;

    private LocalDateTime heureDebut;

    private LocalDateTime heureFin;

    private EtatSeance etat;

    private String salle;

    private String contenu;





}
