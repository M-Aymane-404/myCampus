package com.mycampus.backend.dto.administrateur.DonneSortie.TableauBordDTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableauBordAdministratifDTO {
    private StatistiquesGlobalesDTO statistiquesGlobales;
    private AlertesTableauBordDTO alertes;
    private List<StatistiquesParFiliereDTO> etudiantsParFiliere;
}
