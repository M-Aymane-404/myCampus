package com.mycampus.backend.dto.annonces;

import com.mycampus.backend.entites.Departement;
import com.mycampus.backend.enumerations.AnnonceCibleType;
import com.mycampus.backend.enumerations.TypeAnnonce;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnnonceSortieDTO {
    
    private Long id;
    private String titre;
    private String contenu;
    private LocalDateTime datePublication;
    private boolean urgent;
    private TypeAnnonce typeAnnonce;
     private boolean publiee;
    private AnnonceCibleType cibleType;
    private AuteurDTO auteur;
    private ClasseDTO classe;
    private DepartementDTO departement;
    

}
