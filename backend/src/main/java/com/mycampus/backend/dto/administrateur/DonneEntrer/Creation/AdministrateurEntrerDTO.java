package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data

public class AdministrateurEntrerDTO extends UtilisateurEntrerDto {

    @NotBlank(message = "PPR obligatoire")
    @Pattern(regexp = "^[0-9]{8}$", message = "PPR doit contenir 8 chiffres")
    private String ppr;

    @NotBlank(message = "Grade obligatoire")
    private String grade;

    private String fonction;
}
