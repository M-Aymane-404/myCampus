package com.mycampus.backend.metier.administrateur.utilisateurs;

import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.AdministrateurEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.EtudiantEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.ProfesseurEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.AdministrateurConnecterModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.AdministrateurModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.EtudiantModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.ProfesseurModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.AdministrateurSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ProfesseurSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.EtudiantSortieDTO;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.exceptions.UtilisateursException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GestionUtilisateursMetier {
    
    // ==================== ADMINISTRATEUR ====================
    
    AdministrateurSortieDTO creerAdministrateur(AdministrateurEntrerDTO administrateurEntrerDTO) throws UtilisateursException;
    
    AdministrateurSortieDTO trouverAdministrateurParPpr(String ppr) throws UtilisateursException;
    
    List<AdministrateurSortieDTO> listerTousLesAdministrateurs() throws UtilisateursException;
    
    AdministrateurSortieDTO modifierAdministrateur(String ppr, AdministrateurModificationDTO administrateurModificationDTO) throws UtilisateursException;

    AdministrateurSortieDTO modifierAdministrateurConnecter(String email, AdministrateurConnecterModificationDTO administrateurModificationDTO) throws UtilisateursException;

    void supprimerAdministrateur(String ppr) throws UtilisateursException;
    
    void activerAdministrateur(String ppr) throws UtilisateursException;
    
    void desactiverAdministrateur(String ppr) throws UtilisateursException;
    
    // ==================== PROFESSEUR ====================
    
    ProfesseurSortieDTO creerProfesseur(ProfesseurEntrerDTO professeurEntrerDTO) throws UtilisateursException;
    
    ProfesseurSortieDTO trouverProfesseurParPpr(String ppr) throws UtilisateursException;
    
    List<ProfesseurSortieDTO> listerTousLesProfesseurs() throws UtilisateursException;
    
    ProfesseurSortieDTO modifierProfesseur(String ppr, ProfesseurModificationDTO professeurModificationDTO) throws UtilisateursException;
    
    void supprimerProfesseur(String ppr) throws UtilisateursException;
    
    void activerProfesseur(String ppr) throws UtilisateursException;
    
    void desactiverProfesseur(String ppr) throws UtilisateursException;
    
    // ==================== ETUDIANT ====================
    
    EtudiantSortieDTO creerEtudiant(EtudiantEntrerDTO etudiantEntrerDTO) throws UtilisateursException;
    
    EtudiantSortieDTO trouverEtudiantParApogee(String apogee) throws UtilisateursException;
    
    List<EtudiantSortieDTO> listerTousLesEtudiants() throws UtilisateursException;
    
    EtudiantSortieDTO modifierEtudiant(String apogee, EtudiantModificationDTO etudiantModificationDTO) throws UtilisateursException;
    
    void supprimerEtudiant(String apogee) throws UtilisateursException;
    
    void activerEtudiant(String apogee) throws UtilisateursException;
    
    void desactiverEtudiant(String apogee) throws UtilisateursException;

    @Transactional
    Utilisateur changerPhotoProfil(String email, MultipartFile photo) throws UtilisateursException;
}
