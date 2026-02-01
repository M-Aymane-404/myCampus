package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class MatiereModificationDTO {
    
    @Size(min = 2, max = 100, message = "Nom doit contenir entre 2 et 100 caractères")
    private String nom;
    
    @Size(min = 5, max = 500, message = "Description doit contenir entre 5 et 500 caractères")
    private String description;
    



    private String codeModule;
}
