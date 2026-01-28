package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;

import com.mycampus.backend.enumerations.EtatStructure;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class FiliereModificationDTO {

    @Size(min = 2, max = 100, message = "Nom doit contenir entre 2 et 100 caractères")
    private String nom;
    
    @Size(min = 5, max = 500, message = "Description doit contenir entre 5 et 500 caractères")
    private String description;
    
    @Size(min = 2, max = 50, message = "Durée formation doit contenir entre 2 et 50 caractères")
    private String dureeFormation;
    
    @Size(min = 2, max = 50, message = "Niveau diplôme doit contenir entre 2 et 50 caractères")
    private String niveauDiplome;
    
    private EtatStructure etat;
    
    private String codeDepartement;
    
    @Pattern(regexp = "^[0-9]{8}$", message = "PPR chef filière doit contenir 8 chiffres")
    private String chefFilierePpr;
}
