package com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote;

 import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDeReleveEtudiantDTO {
    private Long id;

    private String nomDuModule;
    private String codeModule;
    private  Integer coefficient;
    List<MatiereDeReleveEtudiantDTO> matieres;
    private Double moyenne;
}
