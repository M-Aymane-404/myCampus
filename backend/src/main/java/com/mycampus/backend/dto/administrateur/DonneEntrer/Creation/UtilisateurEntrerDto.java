package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

public class UtilisateurEntrerDto {


    @NotBlank(message = "Nom obligatoire")
    @Size(min = 2 , max = 50)
    private String nom;

    @NotBlank(message = "Prénom obligatoire")
    @Size(min = 2 , max = 50)
    private String prenom;

    @NotBlank(message = "Email obligatoire")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "mote de passe obligatoire")
    private String password;

    @NotBlank(message = "CIN obligatoire")
    @Pattern(
            regexp = "^[A-Za-z0-9]{4,8}$",
            message = "Numéro de CIN non valide"
    )
    private String cin;

    @NotBlank(message = "Numéro Telephone obligatoire")
    @Size(min = 10 , max = 20, message = "Numéro de Téléphone non valide")
    private String tel;




    private Boolean active ;



}
