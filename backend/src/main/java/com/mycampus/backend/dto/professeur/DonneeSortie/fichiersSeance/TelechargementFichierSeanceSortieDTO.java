package com.mycampus.backend.dto.professeur.DonneeSortie.fichiersSeance;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelechargementFichierSeanceSortieDTO {

    private Long id;

    private String nomOriginal;

    private String typeMime;

    private Long taille;

    private String chemin;

}
