package com.mycampus.backend.dto.professeur.DonneeSortie.presences;

import com.mycampus.backend.enumerations.StatutJustificatif;
import com.mycampus.backend.enumerations.StatusAbsence;
import com.mycampus.backend.enumerations.TypeMotif;
import com.mycampus.backend.enumerations.TypeSeance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JustificatifProfesseurSortieDTO {
    private Long presenceId;
    private Long seanceId;
    private Long idEtudiant;

    private String apogee;
    private String nomEtudiant;
    private String prenomEtudiant;

    private String codeClasse;
    private String nomClasse;
    private String codeMatiere;
    private String nomMatiere;

    private TypeSeance typeSeance;
    private LocalDate dateAbsence;
    private LocalDateTime heureDebut;
    private LocalDateTime heureFin;
    private String salle;

    private StatusAbsence status;
    private StatutJustificatif statutJustificatif;
    private boolean justifiee;
    private boolean justificationSoumise;

    private String motif;
    private TypeMotif typeMotif;
    private String motifUrl;
    private String motifNom;
    private LocalDateTime dateDeclaration;
}
