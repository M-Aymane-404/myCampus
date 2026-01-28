package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;

import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.UtilisateurEntrerDto;
import com.mycampus.backend.enumerations.EtatInscription;
import com.mycampus.backend.enumerations.Semestre;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
            message = "Code Apogée doit contenir 9 chiffres"
    )
    private String apogee;
    
    @Size(max = 50, message = "Niveau ne doit pas dépasser 50 caractères")
    private String niveau;
    
    @Min(value = 2026, message = "Année d'inscription minimum est 2020")
    private Integer anneeInscription;
    
    private Semestre semestreActuel;

    private String anneeFinEtude;
    
    private LocalDate dateNaissance;
    
    private EtatInscription etatInscription;

    private String codeClasse;
}
