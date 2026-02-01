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
    List<ExamenDeReleveEtudiantDTO> examens;
    private Double moyenne;
    private Boolean validee; // moyenne >= 10

}
