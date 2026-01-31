package com.mycampus.backend.dto.etudiant.DonneeSortie;

import lombok.*;

import java.time.LocalDateTime;

 @Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class FichierSeanceSortieEtudiantDTO {
    

    private Long id;
    

    private String nomOriginal;
    

    private String chemin;
    

    private String typeMime;
    

    private Long taille;
    

    private LocalDateTime dateUpload;
    

}
