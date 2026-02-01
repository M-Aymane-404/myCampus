package com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote;

import com.mycampus.backend.enumerations.TypeExamen;
import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamenDeReleveEtudiantDTO {

    private Long id;
    private Integer numeroExamen;
    private TypeExamen typeExamen;
    private Double note;
    private String corrigePath;
    private String copieEtudiantPath;

}
