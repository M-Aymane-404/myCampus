package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;

import com.mycampus.backend.enumerations.EtatStructure;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class DepartementModificationDTO {

    @Size(min = 2, max = 100, message = "Type département doit contenir entre 2 et 100 caractères")
    private String typeDepartement;
    
    @Size(min = 5, max = 500, message = "Description doit contenir entre 5 et 500 caractères")
    private String description;
    
    @Pattern(regexp = "^[0-9]{8}$", message = "PPR chef département doit contenir 8 chiffres")
    private String chefDepartementPpr;
    
    private LocalDate dateCreation;
    
    private EtatStructure etat;
}
