package com.mycampus.backend.metier.security;

import com.mycampus.backend.dto.securite.entree.RafraichirTokenDTO;
import com.mycampus.backend.entites.RefreshToken;

public interface RefreshTokenMetier {
    public RefreshToken creerRefreshToken(String email, String userAgent, String ip) ;
    public RefreshToken  verifierRefreshToken(String token) ;
    public RefreshToken rotationRefreshToken(String tokenActuel, String userAgent, String ip) ;
    public void revoquer(RafraichirTokenDTO refreshTokenDTO) ;

    }
