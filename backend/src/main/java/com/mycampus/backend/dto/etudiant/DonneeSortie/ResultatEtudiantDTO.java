package com.mycampus.backend.dto.etudiant.DonneeSortie;

import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Examen;
import lombok.*;

import java.time.LocalDateTime;

 @Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ResultatEtudiantDTO {
    

    private Long id;
    

    private Double note;
    

    private LocalDateTime datePublication;
    

    private Boolean valide ;
    
    private String copieEtudiantPath;
    

    private Examen examen;

    private Etudiant etudiant;
}
