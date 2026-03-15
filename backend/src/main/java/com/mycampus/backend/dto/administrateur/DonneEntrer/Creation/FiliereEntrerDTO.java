package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import com.mycampus.backend.enumerations.EtatStructure;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class FiliereEntrerDTO {

    @NotBlank(message = "Code filiere obligatoire")
    @Pattern(
            regexp = "^[A-Z0-9]{2,10}$",
            message = "Code filiere doit contenir 2-10 caracteres alphanumeriques en majuscules"
    )
    private String codeFiliere;

    @NotBlank(message = "Nom filiere obligatoire")
    @Size(max = 100, message = "Nom filiere ne doit pas depasser 100 caracteres")
    private String nom;

    @Size(max = 500, message = "Description ne doit pas depasser 500 caracteres")
    private String description;

    @NotNull(message = "Duree formation obligatoire")
    @Min(value = 1, message = "Duree formation doit etre >= 1 an")
    @Max(value = 10, message = "Duree formation doit etre <= 10 ans")
    private Integer dureeFormation;

    @Size(max = 50, message = "Niveau diplome ne doit pas depasser 50 caracteres")
    private String niveauDiplome;

    private EtatStructure etat;

    @Pattern(
            regexp = "^[A-Z0-9]{2,10}$",
            message = "Code departement doit contenir 2-10 caracteres alphanumeriques en majuscules"
    )
    private String codeDepartement;

    @Pattern(
            regexp = "^[0-9]{8}$",
            message = "PPR du chef de filiere doit contenir 8 chiffres"
    )
    private String chefFilierePpr;
}
