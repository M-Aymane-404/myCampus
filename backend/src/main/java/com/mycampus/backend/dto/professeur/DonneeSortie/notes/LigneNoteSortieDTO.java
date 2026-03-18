package com.mycampus.backend.dto.professeur.DonneeSortie.notes;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class LigneNoteSortieDTO {

        private Long idResultat;   //null si pas encore en  db
        private Long idEtudiant;

        private String apogee;
        private String nom;
        private String prenom;

        private Double note;       // null = non saisi
        private boolean publiee;// true si datePublication != null
        private boolean copieDisponible;
        private String copieEtudiantPath;
}
