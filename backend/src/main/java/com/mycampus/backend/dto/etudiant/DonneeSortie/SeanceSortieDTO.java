package com.mycampus.backend.dto.etudiant.DonneeSortie;

import com.mycampus.backend.dto.administrateur.DonneSortie.ModuleSortieDTO;
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
public class SeanceSortieDTO {
    
    private Long id;
    
    private LocalDateTime date;
    
    private String heureDebut;
    
    private String heureFin;
    
    private String salle;
    
    private TypeSeance type;
    
    private String etat;
    
    private String contenu;
    
    private Long affectationEnseignementId;
    private String codeClasse;
    private String professeurPpr;
    private String professeurNom;
    private String professeurPrenom;
    private String codeMatiere;

}
