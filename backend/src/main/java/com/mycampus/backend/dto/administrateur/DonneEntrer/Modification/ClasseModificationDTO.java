package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class ClasseModificationDTO {
    

    private String nom;
    private String niveau;
    private Integer effectifMax;
    private String codeFiliere;
    private String anneeUniversitaire;
    private String emploiTemps;

}
