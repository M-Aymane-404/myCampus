package com.mycampus.backend.dto.etudiant.DonneeSortie;

import com.mycampus.backend.enumerations.EtatExamen;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.TypeExamen;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ExamenSortieEtudiantDTO {
    
      private Long id;

      private Semestre semestre;

    
     private Date dateExamen;
    
     private Integer numeroExamen;

     private TypeExamen typeExamen;
    
     private Integer duree;
    

    private EtatExamen etat;
    
    private String corrigePath;
    

}
