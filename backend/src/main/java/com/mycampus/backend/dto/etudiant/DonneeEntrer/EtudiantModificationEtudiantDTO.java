package com.mycampus.backend.dto.etudiant.DonneeEntrer;

import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.UtilisateurModificationDto;
import com.mycampus.backend.enumerations.EtatInscription;
import com.mycampus.backend.enumerations.Semestre;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

 @Data
@NoArgsConstructor
@AllArgsConstructor
 public class EtudiantModificationEtudiantDTO     {
     @Size(min = 2, max = 50, message = "Nom doit contenir entre 2 et 50 caractères")
     private String nom;

     @Size(min = 2, max = 50, message = "Prénom doit contenir entre 2 et 50 caractères")
     private String prenom;

     @Email(message = "Email invalide")
     @Size(max = 100, message = "Email ne doit pas dépasser 100 caractères")
     private String email;

     @Size(min = 8, max = 100, message = "Mot de passe doit contenir entre 8 et 100 caractères")
     private String ancienPassword;
     @Size(min = 8, max = 100, message = "Mot de passe doit contenir entre 8 et 100 caractères")
     private String nouveauPassword;



     @Size(min = 10, max = 20, message = "Numéro de Téléphone non valide")
     private String tel;







    
    private LocalDate dateNaissance;
    

}
