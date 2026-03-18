package com.mycampus.backend.dto.annonces;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelechargementRessourceAnnonceSortieDTO {

    private Long id;

    private String nomOriginal;

    private String typeMime;

    private Long taille;

    private String chemin;
}
