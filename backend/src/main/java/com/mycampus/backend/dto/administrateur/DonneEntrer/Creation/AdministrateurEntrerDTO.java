package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Pattern(
            regexp = "^[0-9]{8}$",
            message = "PPR doit contenir 8 chiffres"
    )
    private String ppr;

    @Size(max = 50, message = "Grade ne doit pas dépasser 50 caractères")
    private String grade;

    @Size(max = 100, message = "Fonction ne doit pas dépasser 100 caractères")
    private String fonction;
}
