package com.mycampus.backend.metier.security;

import com.mycampus.backend.dto.securite.entree.RafraichirTokenDTO;
import com.mycampus.backend.dto.securite.sortie.RotationResult;
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

@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenMetierImpl implements  RefreshTokenMetier {
    private final RefreshTokenRepository refreshTokenRepository;
    final UtilisateurRepository utilisateurRepository;
    private final StructureSecuriteMapper mapper;

    private static final int REFRESH_TOKEN_DUREE_JOURS = 7;
    private static final int MAX_TOKENS_ACTIFS = 5;



    @Override


    public RefreshToken creerRefreshToken(String email, String userAgent, String ip, String tokenFamily) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new SecuriteException("Utilisateur introuvable"));

        limiterTokensActifs(utilisateur.getId());

        String tokenBrut = genererTokenSecurise();
        String tokenHash = sha256(tokenBrut);

        RefreshToken rt = RefreshToken.builder()
                .utilisateur(utilisateur)
                .tokenHash(tokenHash)
                .tokenFamily(tokenFamily)
                .dateCreation(LocalDateTime.now())
                .dateExpiration(LocalDateTime.now().plusDays(REFRESH_TOKEN_DUREE_JOURS))
                .revoque(false)
                .userAgent(userAgent)
                .ip(ip)
                .build();

        RefreshToken saved = refreshTokenRepository.save(rt);
        saved.setToken(tokenBrut);
        return saved;
    }

    @Override
    public RefreshToken creerRefreshTokenPourlaPremierFois(String email, String userAgent, String ip) {
        return creerRefreshToken(email, userAgent, ip, genererFamilyId());
    }


    @Override

    public RefreshToken verifierRefreshToken(String tokenBrut) throws SecuriteException {
        String tokenHash = sha256(tokenBrut);

        RefreshToken rt = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new SecuriteException("Refresh token invalide"));

        if (Boolean.TRUE.equals(rt.getRevoque())) {
            if (rt.getRemplaceParHash() != null) {

                refreshTokenRepository.revokeFamily(rt.getUtilisateur().getId(), rt.getTokenFamily());
                throw new SecuriteException("Refresh token réutilisé (reuse détecté) -> famille révoquée");
            }

            throw new SecuriteException("Refresh token révoqué");
        }

        if (rt.getDateExpiration() != null && rt.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new SecuriteException("Refresh token expiré");
        }

        return rt;
    }

    @Override
    public RotationResult rotationRefreshToken(String tokenActuel, String userAgent, String ip) {

        RefreshToken ancien = verifierRefreshToken(tokenActuel);
        String email = ancien.getUtilisateur().getEmail();

        //  cree nouveau nouveau token
        String newTokenBrut = genererTokenSecurise();
        String newHash = sha256(newTokenBrut);

        //  revoquer ancien token
        int updated = refreshTokenRepository.revokeIfValid(ancien.getTokenHash(), newHash, LocalDateTime.now());

        if (updated == 0) {
            refreshTokenRepository.revokeFamily(ancien.getUtilisateur().getId(), ancien.getTokenFamily());
            throw new SecuriteException("Refresh token réutilisé (reuse détecté) -> famille révoquée");

        }


         creerRefreshTokenAvecValeur(
                ancien.getUtilisateur(),
                userAgent,
                ip,
                ancien.getTokenFamily(),
                newTokenBrut,
                newHash
        );

        return new RotationResult(email,newTokenBrut);
     }










    @Override

    public void revoquer(RafraichirTokenDTO refreshTokenDTO) throws SecuriteException {
        String tokenHash = sha256(refreshTokenDTO.getRefreshToken());
        RefreshToken rt = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new SecuriteException("Refresh token invalide"));
        rt.setRevoque(true);
        refreshTokenRepository.save(rt);
    }


    @Override
    public void revoquerFamille(String refreshTokenBrut) {

        String tokenHash = sha256(refreshTokenBrut);
        RefreshToken rt = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new SecuriteException("Refresh token invalide"));
        refreshTokenRepository.revokeFamily(rt.getUtilisateur().getId(), rt.getTokenFamily());
    }


    private RefreshToken creerRefreshTokenAvecValeur(
            Utilisateur utilisateur,
            String userAgent,
            String ip,
            String tokenFamily,
            String tokenBrut,
            String tokenHash
    ) {
        RefreshToken rt = RefreshToken.builder()
                .utilisateur(utilisateur)
                .tokenHash(tokenHash)
                .tokenFamily(tokenFamily)
                .dateCreation(LocalDateTime.now())
                .dateExpiration(LocalDateTime.now().plusDays(REFRESH_TOKEN_DUREE_JOURS))
                .revoque(false)
                .userAgent(userAgent)
                .ip(ip)
                .build();

        RefreshToken saved = refreshTokenRepository.save(rt);
        saved.setToken(tokenBrut);
        return saved;
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

    private String genererFamilyId() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

    }
}
