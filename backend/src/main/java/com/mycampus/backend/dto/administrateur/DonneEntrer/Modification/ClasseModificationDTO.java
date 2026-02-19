package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;

import com.mycampus.backend.enumerations.NiveauAnnee;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

    @Size(min = 2, max = 100, message = "Nom doit contenir entre 2 et 100 caractères")
    private String nom;


    @NotBlank(message = "Niveau classe obligatoire")
    private NiveauAnnee niveau;
    
    @Min(value = 1, message = "Effectif minimum est 1")
    @Max(value = 200, message = "Effectif maximum est 200")
    private Integer effectifMax;

    private String codeFiliere;
    
    @Size(min = 9, max = 9, message = "Année universitaire invalide")
    private String anneeUniversitaire;
    
    private String emploiTemps;
}
