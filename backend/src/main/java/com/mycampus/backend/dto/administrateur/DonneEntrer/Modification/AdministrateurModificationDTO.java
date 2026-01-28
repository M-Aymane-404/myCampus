package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;

import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.UtilisateurEntrerDto;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data

public class AdministrateurModificationDTO extends UtilisateurModificationDto {

    @Pattern(regexp = "^[0-9]{8}$", message = "PPR doit contenir 8 chiffres")
    private String ppr;

    @Size(min = 2, max = 50, message = "Grade doit contenir entre 2 et 50 caractères")
    private String grade;

    @Size(min = 2, max = 100, message = "Fonction doit contenir entre 2 et 100 caractères")
    private String fonction;
}
