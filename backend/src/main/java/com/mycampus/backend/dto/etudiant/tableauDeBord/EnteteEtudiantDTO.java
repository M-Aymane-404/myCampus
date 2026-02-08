package com.mycampus.backend.dto.etudiant.tableauDeBord;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnteteEtudiantDTO {
    
    private String nom;
    private String niveau;
    private String filiere;
    private String classe;
    private String session;
    private String statut;
}
