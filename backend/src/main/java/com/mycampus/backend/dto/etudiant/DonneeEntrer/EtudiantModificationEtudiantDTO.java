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



     @Size(min = 8, max = 100, message = "Mot de passe doit contenir entre 8 et 100 caractères")
     private String ancienPassword;
     @Size(min = 8, max = 100, message = "Mot de passe doit contenir entre 8 et 100 caractères")
     private String nouveauPassword;



     @Size(min = 10, max = 20, message = "Numéro de Téléphone non valide")
     private String tel;







    


}
