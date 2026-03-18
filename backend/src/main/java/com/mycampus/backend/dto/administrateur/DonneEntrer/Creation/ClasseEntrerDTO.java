package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import com.mycampus.backend.enumerations.NiveauAnnee;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ClasseEntrerDTO {

    @NotBlank(message = "Code classe obligatoire")
    @Pattern(
            regexp = "^[A-Z0-9]{2,20}$",
            message = "Code classe doit contenir 2-20 caracteres alphanumeriques en majuscules"
    )
    private String codeClasse;

    @NotBlank(message = "Nom classe obligatoire")
    @Size(max = 100, message = "Nom classe ne doit pas depasser 100 caracteres")
    private String nom;

    @NotNull(message = "Niveau classe obligatoire")
    private NiveauAnnee niveau;

    private Integer effectifMax;

    @Pattern(
            regexp = "^[A-Z0-9]{2,10}$",
            message = "Code filiere doit contenir 2-10 caracteres alphanumeriques en majuscules"
    )
    private String codeFiliere;

    @Pattern(
            regexp = "^\\d{4}-\\d{4}$",
            message = "Annee universitaire doit etre au format AAAA-AAAA"
    )
    private String anneeUniversitaire;
}
