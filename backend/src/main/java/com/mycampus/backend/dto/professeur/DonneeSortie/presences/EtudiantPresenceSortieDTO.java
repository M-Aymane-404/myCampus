package com.mycampus.backend.dto.professeur.DonneeSortie.presences;

import lombok.*;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor @Builder
public class EtudiantPresenceSortieDTO {
     private Long id;

     private String nom;
     private String prenom;
     private String apogee;


}
