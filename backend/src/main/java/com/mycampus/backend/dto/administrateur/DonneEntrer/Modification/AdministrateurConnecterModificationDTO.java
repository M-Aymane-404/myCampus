package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

 @AllArgsConstructor
@NoArgsConstructor
 @Getter
 @Setter

public class AdministrateurConnecterModificationDTO     {






     @Size(min = 8, max = 100, message = "Mot de passe doit contenir entre 8 et 100 caractères")
     private String ancienPassword;
    @Size(min = 8, max = 100, message = "Mot de passe doit contenir entre 8 et 100 caractères")
    private String nouveauPassword;



    @Size(min = 10, max = 20, message = "Numéro de Téléphone non valide")
    private String tel;





}
