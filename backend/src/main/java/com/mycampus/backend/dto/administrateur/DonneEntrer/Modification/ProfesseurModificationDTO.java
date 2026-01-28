package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;


import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.UtilisateurEntrerDto;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProfesseurModificationDTO extends UtilisateurModificationDto {
    
    @Pattern(
            regexp = "^[0-9]{8}$",
            message = "PPR doit contenir 8 chiffres"
    )
    private String ppr;

    @Size(max = 100, message = "Spécialité ne doit pas dépasser 100 caractères")
    private String specialite;

    @Size(max = 50, message = "Grade ne doit pas dépasser 50 caractères")
    private String grade;

    private LocalDate dateRecrutement;

    @Size(max = 50, message = "Statut ne doit pas dépasser 50 caractères")
    private String statut;

    @Size(max = 20, message = "Code département ne doit pas dépasser 20 caractères")
    private String codeDepartementOuTravail;
}
