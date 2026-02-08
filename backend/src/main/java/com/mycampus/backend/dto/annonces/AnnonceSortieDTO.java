package com.mycampus.backend.dto.annonces;

import com.mycampus.backend.enumerations.AnnonceCibleType;

import java.time.LocalDateTime;

public class AnnonceSortieDTO {
    
    private Long id;
    private String titre;
    private String contenu;
    private LocalDateTime datePublication;
    private LocalDateTime dateExpiration;
    private boolean publiee;
    private AnnonceCibleType cibleType;
    private AuteurDTO auteur;
    private ClasseDTO classe;
    
    public AnnonceSortieDTO() {}
    
    public AnnonceSortieDTO(Long id, String titre, String contenu, LocalDateTime datePublication,
                           LocalDateTime dateExpiration, boolean publiee, AnnonceCibleType cibleType,
                           AuteurDTO auteur, ClasseDTO classe) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.datePublication = datePublication;
        this.dateExpiration = dateExpiration;
        this.publiee = publiee;
        this.cibleType = cibleType;
        this.auteur = auteur;
        this.classe = classe;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public LocalDateTime getDatePublication() {
        return datePublication;
    }
    
    public void setDatePublication(LocalDateTime datePublication) {
        this.datePublication = datePublication;
    }
    
    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }
    
    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }
    
    public boolean isPubliee() {
        return publiee;
    }
    
    public void setPubliee(boolean publiee) {
        this.publiee = publiee;
    }
    
    public AnnonceCibleType getCibleType() {
        return cibleType;
    }
    
    public void setCibleType(AnnonceCibleType cibleType) {
        this.cibleType = cibleType;
    }
    
    public AuteurDTO getAuteur() {
        return auteur;
    }
    
    public void setAuteur(AuteurDTO auteur) {
        this.auteur = auteur;
    }
    
    public ClasseDTO getClasse() {
        return classe;
    }
    
    public void setClasse(ClasseDTO classe) {
        this.classe = classe;
    }
    
    public static class AuteurDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        
        public AuteurDTO() {}
        
        public AuteurDTO(Long id, String nom, String prenom, String email) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.email = email;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        
        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class ClasseDTO {
        private Long id;
        private String codeClasse;
        private String nom;
        private String niveau;
        
        public ClasseDTO() {}
        
        public ClasseDTO(Long id, String codeClasse, String nom, String niveau) {
            this.id = id;
            this.codeClasse = codeClasse;
            this.nom = nom;
            this.niveau = niveau;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getCodeClasse() { return codeClasse; }
        public void setCodeClasse(String codeClasse) { this.codeClasse = codeClasse; }
        
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        
        public String getNiveau() { return niveau; }
        public void setNiveau(String niveau) { this.niveau = niveau; }
    }
}
