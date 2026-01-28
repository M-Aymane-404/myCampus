package com.mycampus.backend.dto.etudiant.DonneeSortie;

import com.mycampus.backend.entites.Seance;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

 @Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class FichierSeanceSortieDTO {
    

    private Long id;
    

    private String nomOriginal;
    

    private String chemin;
    

    private String typeMime;
    

    private Long taille;
    

    private LocalDateTime dateUpload;
    

}
