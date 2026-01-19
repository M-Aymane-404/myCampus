package com.mycampus.backend.dto.professeur.DonneeSortie.fichiersSeance;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichierSeanceSortieDTO {

    private Long id;

    private Long seanceId;

    private String nomOriginal;

    private String typeMime;

    private Long taille;

    private LocalDateTime dateUpload;
}
