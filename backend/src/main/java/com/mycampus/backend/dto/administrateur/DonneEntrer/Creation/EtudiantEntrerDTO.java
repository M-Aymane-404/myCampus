package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import com.mycampus.backend.enumerations.EtatInscription;
import com.mycampus.backend.enumerations.Semestre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class EtudiantEntrerDTO extends UtilisateurEntrerDto {
    
    @NotBlank(message = "Code apogée obligatoire")
    @Pattern(regexp = "^[0-9]{9}$", message = "Code apogée doit contenir 9 chiffres")
    private String apogee;
    
    @NotBlank(message = "Niveau obligatoire")
    private String niveau;
    
    @NotNull(message = "Année d'inscription obligatoire")
    private Integer anneeInscription;
    
    private Semestre semestreActuel;
    private String anneeFinEtude;
    
    @NotNull(message = "Date de naissance obligatoire")
    private LocalDate dateNaissance;
    
    @NotNull(message = "État d'inscription obligatoire")
    private EtatInscription etatInscription;
    
    @NotBlank(message = "Code classe obligatoire")
    private String codeClasse;
}
