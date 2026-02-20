package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import com.mycampus.backend.enumerations.EtatInscription;
import com.mycampus.backend.enumerations.NiveauAnnee;
import com.mycampus.backend.enumerations.Semestre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class EtudiantEntrerDTO extends UtilisateurEntrerDto {
    
    @NotBlank(message = "Code Apogée obligatoire")
    @Pattern(
            regexp = "^[0-9]{9}$",
            message = "Code Apogée doit contenir 9 chiffres"
    )
    private String apogee;
    
    @Size(max = 50, message = "Niveau ne doit pas dépasser 50 caractères")
    private NiveauAnnee niveau;
    
    private Integer anneeInscription;
    
    private Semestre semestreActuel;
    
    @Size(max = 20, message = "Année fin étude ne doit pas dépasser 20 caractères")
    private String anneeFinEtude;
    
    @NotNull(message = "Date de naissance obligatoire")
    private LocalDate dateNaissance;
    
    private EtatInscription etatInscription;
    
    @Size(max = 20, message = "Code classe ne doit pas dépasser 20 caractères")
    private String codeClasse;
}
