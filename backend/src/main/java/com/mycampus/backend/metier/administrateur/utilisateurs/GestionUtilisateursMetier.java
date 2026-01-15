package com.mycampus.backend.metier.administrateur.utilisateurs;

import com.mycampus.backend.dto.administrateur.DonneEntrer.AdministrateurEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.EtudiantEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.ProfesseurEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.AdministrateurSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ProfesseurSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.EtudiantSortieDTO;
import com.mycampus.backend.exceptions.UtilisateursException;

import java.util.List;

public interface GestionUtilisateursMetier {
    
    // ==================== ADMINISTRATEUR ====================
    
    AdministrateurSortieDTO creerAdministrateur(AdministrateurEntrerDTO administrateurEntrerDTO) throws UtilisateursException;
    
    AdministrateurSortieDTO trouverAdministrateurParPpr(String ppr) throws UtilisateursException;
    
    List<AdministrateurSortieDTO> listerTousLesAdministrateurs() throws UtilisateursException;
    
    AdministrateurSortieDTO modifierAdministrateur(String ppr, AdministrateurEntrerDTO administrateurEntrerDTO) throws UtilisateursException;
    
    void supprimerAdministrateur(String ppr) throws UtilisateursException;
    
    void activerAdministrateur(String ppr) throws UtilisateursException;
    
    void desactiverAdministrateur(String ppr) throws UtilisateursException;
    
    // ==================== PROFESSEUR ====================
    
    ProfesseurSortieDTO creerProfesseur(ProfesseurEntrerDTO professeurEntrerDTO) throws UtilisateursException;
    
    ProfesseurSortieDTO trouverProfesseurParPpr(String ppr) throws UtilisateursException;
    
    List<ProfesseurSortieDTO> listerTousLesProfesseurs() throws UtilisateursException;
    
    ProfesseurSortieDTO modifierProfesseur(String ppr, ProfesseurEntrerDTO professeurEntrerDTO) throws UtilisateursException;
    
    void supprimerProfesseur(String ppr) throws UtilisateursException;
    
    void activerProfesseur(String ppr) throws UtilisateursException;
    
    void desactiverProfesseur(String ppr) throws UtilisateursException;
    
    // ==================== ETUDIANT ====================
    
    EtudiantSortieDTO creerEtudiant(EtudiantEntrerDTO etudiantEntrerDTO) throws UtilisateursException;
    
    EtudiantSortieDTO trouverEtudiantParApogee(String apogee) throws UtilisateursException;
    
    List<EtudiantSortieDTO> listerTousLesEtudiants() throws UtilisateursException;
    
    EtudiantSortieDTO modifierEtudiant(String apogee, EtudiantEntrerDTO etudiantEntrerDTO) throws UtilisateursException;
    
    void supprimerEtudiant(String apogee) throws UtilisateursException;
    
    void activerEtudiant(String apogee) throws UtilisateursException;
    
    void desactiverEtudiant(String apogee) throws UtilisateursException;
}
