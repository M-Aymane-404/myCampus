package com.mycampus.backend.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
public class TableauBordAdministratifDTO {
    
    private final StatistiquesGlobalesDTO statistiquesGlobales;
    private final List<StatistiquesParFiliereDTO> statistiquesParFiliere;
    private final List<StatistiquesParDepartementDTO> statistiquesParDepartement;
    private final Date dateGeneration;
}
