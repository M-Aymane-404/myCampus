package com.mycampus.backend.dto.administrateur.DonneSortie;

import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.EtatInscription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EtudiantSortieDTO extends UtilisateurSortieDto {
    private String apogee;
    private String niveau;
    private Integer anneeInscription;
    private Semestre semestreActuel;
    private String anneeFinEtude;
    private LocalDate dateNaissance;
    private EtatInscription etatInscription;
    private String codeClasse;
}
