package com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote;

import com.mycampus.backend.enumerations.Semestre;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleveDeNoteEtudiantParSessionDTO {

    private Semestre semestre;


    private Double moyenneSession;
    private Integer nbModules;
    private Integer nbModulesValider;

   private List<ModuleDeReleveEtudiantDTO> modules;
}
