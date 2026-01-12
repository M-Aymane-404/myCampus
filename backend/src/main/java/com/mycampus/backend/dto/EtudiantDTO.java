package com.mycampus.backend.dto;

import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.EtatInscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EtudiantDTO extends UtilisateurDto {
    private String apogee;
    private String niveau;
    private Integer anneeInscription;
    private Semestre semestreActuel;
    private String anneeFinEtude;
    private LocalDate dateNaissance;
    private EtatInscription etatInscription;
    private Long classeId;
}
