package com.mycampus.backend.dto.professeur.DonneeSortie.notes;

import com.mycampus.backend.enumerations.EtatExamen;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.TypeExamen;
import lombok.*;

import java.util.Date;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeuilleNotesSortieDTO {

        private String codeClasse;
        private String nomClasse;

        private String codeMatiere;
        private String nomMatiere;

        private Long idExamen;
        private Integer numeroExamen;
        private TypeExamen typeExamen;
        private Date dateExamen;

        private Semestre semestre;
        private Integer duree;
        private EtatExamen etat;

        private List<LigneNoteSortieDTO> lignes;
        private StatsNotesSortieDTO stats;
    }

