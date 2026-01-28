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
    @Size(max = 100, message = "Email ne doit pas dépasser 100 caractères")
    private String email;

    @Size(min = 8, max = 100, message = "Mot de passe doit contenir entre 8 et 100 caractères")
    private String password;

    @Pattern(
            regexp = "^[A-Za-z0-9]{4,8}$",
            message = "Numéro de CIN non valide"
    )
    private String cin;

    @Size(min = 10, max = 20, message = "Numéro de Téléphone non valide")
    private String tel;

    @Size(max = 50, message = "Photo ne doit pas dépasser 50 caractères")
    private String photo;

    private Boolean active;
}
