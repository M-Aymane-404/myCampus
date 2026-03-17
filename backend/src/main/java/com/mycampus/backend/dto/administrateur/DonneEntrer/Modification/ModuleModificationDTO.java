package com.mycampus.backend.dto.administrateur.DonneEntrer.Modification;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.mycampus.backend.enumerations.Semestre;
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
public class ModuleModificationDTO {
    
    @Size(min = 2, max = 100, message = "Nom doit contenir entre 2 et 100 caractères")
    private String nom;
    
    @Size(min = 5, max = 500, message = "Description doit contenir entre 5 et 500 caractères")
    private String description;
    
    private Semestre semestre;
    

    @JsonAlias({"filiereCode", "code_filiere"})
    private String codeFiliere;

    @JsonAlias({"idFiliere"})
    private String filiereId;

    public void setCodeFiliere(String codeFiliere) {
        if (codeFiliere == null) {
            this.codeFiliere = null;
            return;
        }
        String normalized = codeFiliere.trim();
        this.codeFiliere = normalized.isEmpty() ? null : normalized.toUpperCase();
    }

    public void setFiliereId(String filiereId) {
        if (filiereId == null) {
            this.filiereId = null;
            return;
        }
        String normalized = filiereId.trim();
        this.filiereId = normalized.isEmpty() ? null : normalized;
    }
}
