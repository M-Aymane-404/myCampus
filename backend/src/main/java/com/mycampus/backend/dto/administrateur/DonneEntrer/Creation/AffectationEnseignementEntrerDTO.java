package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import com.mycampus.backend.enumerations.Semestre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AffectationEnseignementEntrerDTO {
    
    private Boolean active;
    
    @NotBlank(message = "PPR professeur obligatoire")
    @Pattern(
            regexp = "^[0-9]{8}$",
            message = "PPR doit contenir 8 chiffres"
    )
    private String professeurPpr;
    
    @NotBlank(message = "Code classe obligatoire")
    @Pattern(
            regexp = "^[A-Z0-9]{2,20}$",
            message = "Code classe doit contenir 2-20 caractères alphanumériques en majuscules"
    )
    private String codeClasse;
    
    @NotBlank(message = "Code matière obligatoire")
    @Pattern(
            regexp = "^[A-Z0-9]{2,20}$",
            message = "Code matière doit contenir 2-20 caractères alphanumériques en majuscules"
    )
    private String codeMatiere;


    @NotBlank(message = "Année universitaire obligatoire")
    @Pattern(regexp = "\\d{4}-\\d{4}",
            message = "Année universitaire  doit etre sous la forme 2025-2026"

    )
    private String anneeUniversitaire;

}
