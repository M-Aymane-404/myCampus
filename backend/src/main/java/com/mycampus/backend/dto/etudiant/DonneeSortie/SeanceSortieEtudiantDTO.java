package com.mycampus.backend.dto.etudiant.DonneeSortie;

import com.mycampus.backend.enumerations.EtatSeance;
import com.mycampus.backend.enumerations.TypeSeance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SeanceSortieEtudiantDTO {
    
    private Long id;
    
    private LocalDateTime date;
    
    private String heureDebut;
    
    private String heureFin;
    
    private String salle;
    
    private TypeSeance type;
    
    private EtatSeance etat;
    
    private String contenu;
    


}
