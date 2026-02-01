package com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote;

import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatiereDeReleveEtudiantDTO {

    private Long id;
    private String nomDuMatiere;
    private String codeMatiere;
    private List<ExamenDeReleveEtudiantDTO> examens;
    private Double moyenneDuMatiere;
    private Boolean validee; // moyenne >= 10

}
