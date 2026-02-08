package com.mycampus.backend.dto.annonces;

import com.mycampus.backend.enumerations.AnnonceCibleType;

import java.time.LocalDateTime;

public class AnnonceSortieEtudiantDTO {
    
    private Long id;
    private String titre;
    private String contenu;
    private LocalDateTime datePublication;
    private LocalDateTime dateExpiration;
    private boolean publiee;
    private AnnonceCibleType cibleType;
    private String codeClasse;
    private AuteurDTO auteur;
    
    public AnnonceSortieEtudiantDTO() {}
    
    public AnnonceSortieEtudiantDTO(Long id, String titre, String contenu, LocalDateTime datePublication,
                                 LocalDateTime dateExpiration, boolean publiee, AnnonceCibleType cibleType,
                                 String codeClasse, AuteurDTO auteur) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.datePublication = datePublication;
        this.dateExpiration = dateExpiration;
        this.publiee = publiee;
        this.cibleType = cibleType;
        this.codeClasse = codeClasse;
        this.auteur = auteur;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    
    public LocalDateTime getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication = datePublication; }
    
    public LocalDateTime getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDateTime dateExpiration) { this.dateExpiration = dateExpiration; }
    
    public boolean isPubliee() { return publiee; }
    public void setPubliee(boolean publiee) { this.publiee = publiee; }
    
    public AnnonceCibleType getCibleType() { return cibleType; }
    public void setCibleType(AnnonceCibleType cibleType) { this.cibleType = cibleType; }
    
    public String getCodeClasse() { return codeClasse; }
    public void setCodeClasse(String codeClasse) { this.codeClasse = codeClasse; }
    
    public AuteurDTO getAuteur() { return auteur; }
    public void setAuteur(AuteurDTO auteur) { this.auteur = auteur; }
    
    public static class AuteurDTO {
        private Long id;
        private String nom;
        private String prenom;
        
        public AuteurDTO() {}
        
        public AuteurDTO(Long id, String nom, String prenom) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        
        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }
    }
}
