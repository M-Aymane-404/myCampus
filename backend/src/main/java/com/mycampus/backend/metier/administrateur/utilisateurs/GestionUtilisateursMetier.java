package com.mycampus.backend.metier.administrateur.utilisateurs;

import com.mycampus.backend.dto.AdministrateurDTO;
import com.mycampus.backend.exceptions.UtilisateursException;

import java.util.List;

public interface GestionUtilisateursMetier {
    
    AdministrateurDTO creerAdministrateur(AdministrateurDTO administrateurDTO) throws UtilisateursException;
    
    AdministrateurDTO trouverAdministrateurParId(Long id) throws UtilisateursException;
    
    AdministrateurDTO trouverAdministrateurParPpr(String ppr) throws UtilisateursException;
    
    List<AdministrateurDTO> listerTousLesAdministrateurs() throws UtilisateursException;
    
    AdministrateurDTO modifierAdministrateur(Long id, AdministrateurDTO administrateurDTO) throws UtilisateursException;
    
    void supprimerAdministrateur(Long id) throws UtilisateursException;
    

    void activerAdministrateur(Long id) throws UtilisateursException;
    
    void desactiverAdministrateur(Long id) throws UtilisateursException;
}
