package com.mycampus.backend.metier.security;

import com.mycampus.backend.dto.securite.entree.RafraichirTokenDTO;
import com.mycampus.backend.dto.securite.sortie.RotationResult;
import com.mycampus.backend.entites.RefreshToken;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenMetier {
    public RefreshToken creerRefreshToken(String email, String userAgent, String ip, String tokenFamily) ;

    RefreshToken creerRefreshTokenPourlaPremierFois(String email, String userAgent, String ip);

    public RefreshToken  verifierRefreshToken(String token) ;
    public RotationResult rotationRefreshToken(String tokenActuel, String userAgent, String ip) ;
    public void revoquer(RafraichirTokenDTO refreshTokenDTO) ;


    void revoquerFamille(String refreshTokenBrut);
}
