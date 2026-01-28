package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;


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
public class ProfesseurEntrerDTO extends UtilisateurEntrerDto {
    
    @NotBlank(message = "PPR obligatoire")
    @Pattern(regexp = "^[0-9]{8}$", message = "PPR doit contenir 8 chiffres")
    private String ppr;

    @NotBlank(message = "Spécialité obligatoire")
    private String specialite;

    @NotBlank(message = "Grade obligatoire")
    private String grade;

    @NotNull(message = "Date de recrutement obligatoire")
    private LocalDate dateRecrutement;

    @NotBlank(message = "Statut obligatoire")
    private String statut;

    private String codeDepartementOuTravail;
}
