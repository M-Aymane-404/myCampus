package com.mycampus.backend.dto.administrateur.DonneEntrer.Creation;

import com.mycampus.backend.enumerations.Semestre;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AffectationEnseignementEntrerDTO {
    
    private Boolean active;
    
    @NotBlank(message = "PPR professeur obligatoire")
    private String professeurPpr;
    
    @NotBlank(message = "Code classe obligatoire")
    private String codeClasse;
    
    @NotBlank(message = "Code matière obligatoire")
    private String codeMatiere;
}
