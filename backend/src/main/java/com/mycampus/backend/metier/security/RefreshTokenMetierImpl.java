package com.mycampus.backend.metier.security;

import com.mycampus.backend.dto.securite.entree.RafraichirTokenDTO;
import com.mycampus.backend.entites.RefreshToken;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.exceptions.SecuriteException;
import com.mycampus.backend.mappers.security.StructureSecuriteMapper;
import com.mycampus.backend.repositorys.RefreshTokenRepository;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenMetierImpl implements  RefreshTokenMetier {
    private final RefreshTokenRepository refreshTokenRepository;
    final UtilisateurRepository utilisateurRepository;
    private final StructureSecuriteMapper mapper;

    // Exemple: 7 jours (tu peux changer)
    private static final int REFRESH_TOKEN_DUREE_JOURS = 7;
    private static final int MAX_TOKENS_ACTIFS = 5;

    @Override
    @Transactional
    public RefreshToken creerRefreshToken(String email, String userAgent, String ip) throws SecuriteException {

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new SecuriteException("Utilisateur introuvable"));
        limiterTokensActifs(utilisateur.getId());


        String tokenBrut = genererTokenSecurise();
        String tokenHash = sha256(tokenBrut);
        RefreshToken rt = RefreshToken.builder()
                .utilisateur(utilisateur)
                .tokenHash(tokenHash)
                .dateCreation(LocalDateTime.now())
                .dateExpiration(LocalDateTime.now().plusDays(REFRESH_TOKEN_DUREE_JOURS))
                .revoque(false)
                .userAgent(userAgent)
                .ip(ip)
                .build();
        RefreshToken saved = refreshTokenRepository.save(rt);
        saved.setToken(tokenBrut);
        return  saved;
    }
    @Override

    public RefreshToken verifierRefreshToken(String tokenBrut) throws SecuriteException {
        String tokenHash = sha256(tokenBrut);

        RefreshToken rt = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new SecuriteException("Refresh token invalide"));

        if (Boolean.TRUE.equals(rt.getRevoque())) {
            revoquerTousLesTokensActifs(rt.getUtilisateur().getId());
            throw new SecuriteException("Refresh token révoqué (compromission détectée)");
        }

        if (rt.getDateExpiration() != null && rt.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new SecuriteException("Refresh token expiré");
        }

        return rt;
    }
    @Override

    @Transactional
    public RefreshToken rotationRefreshToken(String tokenActuel, String userAgent, String ip) {
        RefreshToken ancien = verifierRefreshToken(tokenActuel);
        if (ancien.getUserAgent() != null && !ancien.getUserAgent().equals(userAgent)) {
            throw new SecuriteException("Device non reconnu (User-Agent différent)");
        }
        if (ancien.getIp() != null && !ancien.getIp().equals(ip)) {
            throw new SecuriteException("Device non reconnu (IP différente)");
        }
        ancien.setRevoque(true);
        refreshTokenRepository.save(ancien);

        return creerRefreshToken(ancien.getUtilisateur().getEmail(), userAgent, ip);
    }

    @Override
    @Transactional
    public void revoquer(RafraichirTokenDTO refreshTokenDTO) throws SecuriteException {
        String tokenHash = sha256(refreshTokenDTO.getRefreshToken());
        RefreshToken rt = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new SecuriteException("Refresh token invalide"));
        rt.setRevoque(true);
        refreshTokenRepository.save(rt);
    }

    private void revoquerTousLesTokensActifs(Long utilisateurId) {
        List<RefreshToken> actifs = refreshTokenRepository
                .findAllByUtilisateurIdAndRevoqueFalse(utilisateurId);
        actifs.forEach(t -> t.setRevoque(true));
        refreshTokenRepository.saveAll(actifs);
    }




    private void limiterTokensActifs(Long utilisateurId) {
        List<RefreshToken> actifs = refreshTokenRepository
                .findByUtilisateurIdAndRevoqueFalseOrderByDateCreationAsc(utilisateurId);

        int surplus = actifs.size() - (MAX_TOKENS_ACTIFS - 1);
         if (surplus <= 0) return;

        for (int i = 0; i < surplus; i++) {
            actifs.get(i).setRevoque(true);
        }
        refreshTokenRepository.saveAll(actifs.subList(0, surplus));
    }

    private String genererTokenSecurise() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
