package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;

import com.mycampus.backend.enumerations.Semestre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AffectationEnseignementModificationDTO {
    
    private Boolean active;
    
    @Pattern(regexp = "^[0-9]{8}$", message = "PPR professeur doit contenir 8 chiffres")
    private String professeurPpr;

     private String codeClasse;

     private String codeMatiere;
}
