package com.mycampus.backend.metier.security;

import com.mycampus.backend.exceptions.UtilisateursException;

public interface GestionUtilisateurConnecteMetier {
    public Long trouverIdUtilisateurConnecte(String email) throws UtilisateursException ;

    }
