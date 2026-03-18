package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;

import com.mycampus.backend.enumerations.NiveauAnnee;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class ClasseModificationDTO {

    @Size(min = 2, max = 100, message = "Nom doit contenir entre 2 et 100 caracteres")
    private String nom;

    private NiveauAnnee niveau;

    @Min(value = 1, message = "Effectif minimum est 1")
    @Max(value = 200, message = "Effectif maximum est 200")
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
