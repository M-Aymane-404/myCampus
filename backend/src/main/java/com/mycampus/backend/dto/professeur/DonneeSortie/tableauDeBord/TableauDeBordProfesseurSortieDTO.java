package com.mycampus.backend.dto.professeur.DonneeSortie.tableauDeBord;


import com.mycampus.backend.enumerations.EtatExamen;
import com.mycampus.backend.enumerations.EtatSeance;
import com.mycampus.backend.enumerations.TypeExamen;
import com.mycampus.backend.enumerations.TypeSeance;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableauDeBordProfesseurSortieDTO {

    private EnteteProfesseurDTO entete;
    private StatsRapidesProfesseurDTO stats;
    private List<SeanceResumeDTO>   seancesRecentes;
    private List<ExamenResumeDTO>   examensAVenir;

    // ── Entête ────────────────────────────────────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EnteteProfesseurDTO {
        private String nomComplet;
        private String ppr;
        private String grade;
        private String specialite;
        private String departement;
        private String statut;
    }

    // ── Stats rapides ─────────────────────────────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StatsRapidesProfesseurDTO {
        private int nbEnseignements;
        private int nbSeancesTotales;
        private int nbSeancesTerminees;
        private int nbExamensProgrammes;
        private int nbExamensTotal;
        private int nbAnnoncesPubiiees;
    }

    // ── Séance résumé ─────────────────────────────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SeanceResumeDTO {
        private Long        id;
        private TypeSeance  type;
        private LocalDate   date;
        private LocalDateTime heureDebut;
        private LocalDateTime heureFin;
        private String      salle;
        private EtatSeance  etat;
        private String      nomMatiere;
        private String      nomClasse;
        private String      codeClasse;
    }

    // ── Examen résumé ─────────────────────────────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ExamenResumeDTO {
        private Long          id;
        private String        nomMatiere;
        private String        codeMatiere;
        private String        nomClasse;
        private TypeExamen    typeExamen;
        private int           numeroExamen;
        private Date dateExamen;
        private int           duree;
        private EtatExamen    etat;
    }
}
