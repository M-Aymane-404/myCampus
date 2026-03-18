package com.mycampus.backend.dto.annonces;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RessourceAnnonceSortieDTO {

    private Long id;

    private Long annonceId;

    private String nomOriginal;

    private String typeMime;

    private Long taille;

    private LocalDateTime dateUpload;
}
