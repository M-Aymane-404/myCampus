package com.mycampus.backend.dto.professeur.DonneeEntrer.fichiersSeance;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuppressionFichierSeanceEntrerDTO {

    private Long fichierSeanceId;
}
