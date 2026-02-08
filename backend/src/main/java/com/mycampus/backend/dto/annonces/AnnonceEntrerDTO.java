package com.mycampus.backend.dto.annonces;

import com.mycampus.backend.enumerations.AnnonceCibleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class AnnonceEntrerDTO {
    
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200, message = "Le titre ne doit pas dépasser 200 caractères")
    private String titre;
    
    @NotBlank(message = "Le contenu est obligatoire")
    @Size(max = 2000, message = "Le contenu ne doit pas dépasser 2000 caractères")
    private String contenu;
    
    private LocalDateTime dateExpiration;
    
    @NotNull(message = "Le type de cible est obligatoire")
    private AnnonceCibleType cibleType;
    
    private String codeClasse;
    
    public AnnonceEntrerDTO() {}
    
    public AnnonceEntrerDTO(String titre, String contenu, LocalDateTime dateExpiration, 
                           AnnonceCibleType cibleType, String codeClasse) {
        this.titre = titre;
        this.contenu = contenu;
        this.dateExpiration = dateExpiration;
        this.cibleType = cibleType;
        this.codeClasse = codeClasse;
    }
    
    public String getTitre() {
        return titre;
    }
    
    public void setTitre(String titre) {
        this.titre = titre;
    }
    
    public String getContenu() {
        return contenu;
    }
    
    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
    
    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }
    
    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }
    
    public AnnonceCibleType getCibleType() {
        return cibleType;
    }
    
    public void setCibleType(AnnonceCibleType cibleType) {
        this.cibleType = cibleType;
    }
    
    public String getCodeClasse() {
        return codeClasse;
    }
    
    public void setCodeClasse(String codeClasse) {
        this.codeClasse = codeClasse;
    }
}
