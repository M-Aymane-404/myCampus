package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data

public class UtilisateurModificationDto {

    @Size(min = 2, max = 50, message = "Nom doit contenir entre 2 et 50 caractères")
    private String nom;

    @Size(min = 2, max = 50, message = "Prénom doit contenir entre 2 et 50 caractères")
    private String prenom;

    @Email(message = "Email invalide")
    private String email;

    @Size(min = 8, message = "Mot de passe doit contenir au moins 8 caractères")
    private String password;

    @Pattern(regexp = "^[A-Z0-9]{4,8}$", message = "CIN invalide")
    private String cin;

    @Size(min = 10, max = 20, message = "Numéro de téléphone invalide")
    private String tel;

    @Size(min = 2, max = 50)
    private String photo;

    private Boolean active;
}
