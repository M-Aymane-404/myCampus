package com.mycampus.backend.metier.administrateur.utilisateurs;

import com.mycampus.backend.dto.AdministrateurDTO;
import com.mycampus.backend.dto.ProfesseurDTO;
import com.mycampus.backend.dto.EtudiantDTO;
import com.mycampus.backend.exceptions.UtilisateursException;

import java.util.List;

public interface GestionUtilisateursMetier {
    
    // ==================== ADMINISTRATEUR ====================
    
    AdministrateurDTO creerAdministrateur(AdministrateurDTO administrateurDTO) throws UtilisateursException;
    
    AdministrateurDTO trouverAdministrateurParId(Long id) throws UtilisateursException;
    
    AdministrateurDTO trouverAdministrateurParPpr(String ppr) throws UtilisateursException;
    
    List<AdministrateurDTO> listerTousLesAdministrateurs() throws UtilisateursException;
    
    AdministrateurDTO modifierAdministrateur(Long id, AdministrateurDTO administrateurDTO) throws UtilisateursException;
    
    void supprimerAdministrateur(Long id) throws UtilisateursException;
    
    void activerAdministrateur(Long id) throws UtilisateursException;
    
    void desactiverAdministrateur(Long id) throws UtilisateursException;
    
    // ==================== PROFESSEUR ====================
    
    ProfesseurDTO creerProfesseur(ProfesseurDTO professeurDTO) throws UtilisateursException;
    
    ProfesseurDTO trouverProfesseurParId(Long id) throws UtilisateursException;
    
    ProfesseurDTO trouverProfesseurParPpr(String ppr) throws UtilisateursException;
    
    List<ProfesseurDTO> listerTousLesProfesseurs() throws UtilisateursException;
    
    ProfesseurDTO modifierProfesseur(Long id, ProfesseurDTO professeurDTO) throws UtilisateursException;
    
    void supprimerProfesseur(Long id) throws UtilisateursException;
    
    void activerProfesseur(Long id) throws UtilisateursException;
    
    void desactiverProfesseur(Long id) throws UtilisateursException;
    
    // ==================== ETUDIANT ====================
    
    EtudiantDTO creerEtudiant(EtudiantDTO etudiantDTO) throws UtilisateursException;
    
    EtudiantDTO trouverEtudiantParId(Long id) throws UtilisateursException;
    
    EtudiantDTO trouverEtudiantParApogee(String apogee) throws UtilisateursException;
    
    List<EtudiantDTO> listerTousLesEtudiants() throws UtilisateursException;
    
    EtudiantDTO modifierEtudiant(Long id, EtudiantDTO etudiantDTO) throws UtilisateursException;
    
    void supprimerEtudiant(Long id) throws UtilisateursException;
    
    void activerEtudiant(Long id) throws UtilisateursException;
    
    void desactiverEtudiant(Long id) throws UtilisateursException;
}
