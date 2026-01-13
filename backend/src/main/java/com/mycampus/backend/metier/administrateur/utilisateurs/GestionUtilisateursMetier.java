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
    
    AdministrateurSortieDTO trouverAdministrateurParId(Long id) throws UtilisateursException;
    
    AdministrateurSortieDTO trouverAdministrateurParPpr(String ppr) throws UtilisateursException;
    
    List<AdministrateurSortieDTO> listerTousLesAdministrateurs() throws UtilisateursException;
    
    AdministrateurSortieDTO modifierAdministrateur(Long id, AdministrateurEntrerDTO administrateurEntrerDTO) throws UtilisateursException;
    
    void supprimerAdministrateur(Long id) throws UtilisateursException;
    
    void activerAdministrateur(Long id) throws UtilisateursException;
    
    void desactiverAdministrateur(Long id) throws UtilisateursException;
    
    // ==================== PROFESSEUR ====================
    
    ProfesseurSortieDTO creerProfesseur(ProfesseurEntrerDTO professeurEntrerDTO) throws UtilisateursException;
    
    ProfesseurSortieDTO trouverProfesseurParId(Long id) throws UtilisateursException;
    
    ProfesseurSortieDTO trouverProfesseurParPpr(String ppr) throws UtilisateursException;
    
    List<ProfesseurSortieDTO> listerTousLesProfesseurs() throws UtilisateursException;
    
    ProfesseurSortieDTO modifierProfesseur(Long id, ProfesseurEntrerDTO professeurEntrerDTO) throws UtilisateursException;
    
    void supprimerProfesseur(Long id) throws UtilisateursException;
    
    void activerProfesseur(Long id) throws UtilisateursException;
    
    void desactiverProfesseur(Long id) throws UtilisateursException;
    
    // ==================== ETUDIANT ====================
    
    EtudiantSortieDTO creerEtudiant(EtudiantEntrerDTO etudiantEntrerDTO) throws UtilisateursException;
    
    EtudiantSortieDTO trouverEtudiantParId(Long id) throws UtilisateursException;
    
    EtudiantSortieDTO trouverEtudiantParApogee(String apogee) throws UtilisateursException;
    
    List<EtudiantSortieDTO> listerTousLesEtudiants() throws UtilisateursException;
    
    EtudiantSortieDTO modifierEtudiant(Long id, EtudiantEntrerDTO etudiantEntrerDTO) throws UtilisateursException;
    
    void supprimerEtudiant(Long id) throws UtilisateursException;
    
    void activerEtudiant(Long id) throws UtilisateursException;
    
    void desactiverEtudiant(Long id) throws UtilisateursException;
}
