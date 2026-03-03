package com.mycampus.backend.dto.etudiant.DonneeSortie;

import com.mycampus.backend.enumerations.EtatSeance;
import com.mycampus.backend.enumerations.TypeSeance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SeanceSortieEtudiantDTO {
    
    private Long id;
    
    private LocalDate date;
    
    private LocalDateTime  heureDebut;
    
    private LocalDateTime  heureFin;
    
    private String salle;
    
    private TypeSeance type;
    
    private EtatSeance etat;
    
    private String contenu;
    


}
