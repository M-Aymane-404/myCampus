package com.mycampus.backend.dto.professeur.DonneeEntrer;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProfModificationProfDTO {


    @Size(min = 8, max = 100, message = "Mot de passe doit contenir entre 8 et 100 caractères")
    private String ancienPassword;
    @Size(min = 8, max = 100, message = "Mot de passe doit contenir entre 8 et 100 caractères")
    private String nouveauPassword;



    @Size(min = 10, max = 20, message = "Numéro de Téléphone non valide")
    private String tel;
}
