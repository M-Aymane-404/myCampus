package com.mycampus.backend.dto.etudiant.tableauDeBord;

import com.mycampus.backend.enumerations.NiveauAnnee;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnteteEtudiantDTO {
    
    private String nom;
    private NiveauAnnee niveau;
    private String filiere;
    private String classe;
    private String session;
    private String statut;
}
