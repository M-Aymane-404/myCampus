package com.mycampus.backend.dto.administrateur.DonneSortie.TableauBordDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatistiquesParFiliereDTO {
    private String codeFiliere;
    private String filiere;
    private Long totalEtudiants;
}
