package com.mycampus.backend.dto.annonces;

import com.mycampus.backend.enumerations.AnnonceCibleType;
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
     private boolean publiee;
    private AnnonceCibleType cibleType;
    private AuteurDTO auteur;
    private ClasseDTO classe;
    

}
