package com.mycampus.backend.dto.professeur.DonneeSortie.presences;

import lombok.*;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class EtudiantPresenceSortieDTO {
     private int id;
     private String nom;
     private String prenom;
     private String apogee;
}
