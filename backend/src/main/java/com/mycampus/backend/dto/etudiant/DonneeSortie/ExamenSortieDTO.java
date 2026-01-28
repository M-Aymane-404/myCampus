package com.mycampus.backend.dto.etudiant.DonneeSortie;

import com.mycampus.backend.entites.Matiere;
import com.mycampus.backend.entites.Resultat;
import com.mycampus.backend.enumerations.EtatExamen;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.TypeExamen;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

 @Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ExamenSortieDTO {
    
      private Long id;

      private Semestre semestre;

    
     private Date dateExamen;
    
     private Integer numeroExamen;

     private TypeExamen typeExamen;
    
     private Integer duree;
    

    private EtatExamen etat;
    
    private String corrigePath;
    

}
