package com.mycampus.backend.dto.administrateur.DonneEntrer;

import com.mycampus.backend.enumerations.EtatInscription;
import com.mycampus.backend.enumerations.Semestre;
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
public class EtudiantEntrerDTO extends UtilisateurEntrerDto {
    private String apogee;
    private String niveau;
    private Integer anneeInscription;
    private Semestre semestreActuel;
    private String anneeFinEtude;
    private LocalDate dateNaissance;
    private EtatInscription etatInscription;
    private String codeClasse;
}
