package com.mycampus.backend.dto.etudient.DonneeEntrer;

import com.mycampus.backend.enumerations.TypeSeance;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SeanceDTO {
    
    private Long id;
    
    private LocalDateTime date;
    
    private String heureDebut;
    
    private String heureFin;
    
    private String salle;
    
    private TypeSeance type;
    
    private String etat;
    
    private String contenu;
    
    private Long affectationEnseignementId;
}
