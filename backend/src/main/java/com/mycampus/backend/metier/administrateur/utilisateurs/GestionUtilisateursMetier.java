package com.mycampus.backend.metier.administrateur.utilisateurs;

import com.mycampus.backend.dto.AdministrateurDTO;
import com.mycampus.backend.exceptions.UtilisateursException;

import java.util.List;
import java.util.Optional;

public interface GestionUtilisateursMetier {
    
    AdministrateurDTO creerAdministrateur(AdministrateurDTO administrateurDTO) throws UtilisateursException;
    
    Optional<AdministrateurDTO> trouverAdministrateurParId(Long id);
    
    Optional<AdministrateurDTO> trouverAdministrateurParPpr(String ppr);
    
    List<AdministrateurDTO> listerTousLesAdministrateurs() throws UtilisateursException;
    
    AdministrateurDTO modifierAdministrateur(Long id, AdministrateurDTO administrateurDTO) throws UtilisateursException;
    
    void supprimerAdministrateur(Long id) throws UtilisateursException;
    

    void activerAdministrateur(Long id) throws UtilisateursException;
    
    void desactiverAdministrateur(Long id) throws UtilisateursException;
}
