package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;

import com.mycampus.backend.enumerations.EtatInscription;
import com.mycampus.backend.enumerations.NiveauAnnee;
import com.mycampus.backend.enumerations.Semestre;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
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
public class EtudiantModificationDTO extends UtilisateurModificationDto {

    @Pattern(
            regexp = "^[0-9]{9}$",
            message = "Code Apogee doit contenir 9 chiffres"
    )
    private String apogee;

    private NiveauAnnee niveau;

    @Min(value = 2020, message = "Annee d'inscription minimum est 2020")
    private Integer anneeInscription;

    private Semestre semestreActuel;

    private String anneeFinEtude;

    private LocalDate dateNaissance;

    private EtatInscription etatInscription;

    private String codeClasse;
}
