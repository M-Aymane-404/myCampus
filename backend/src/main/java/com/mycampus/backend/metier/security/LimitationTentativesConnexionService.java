package com.mycampus.backend.metier.security;

import com.mycampus.backend.exceptions.TropDeTentativesException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LimitationTentativesConnexionService {

    private static final int MAX_TENTATIVES = 5;
    private static final Duration FENETRE = Duration.ofMinutes(15);
    private static final Duration BLOCAGE = Duration.ofMinutes(15);

    private final Map<String, TentativesConnexion> tentativesParCle = new ConcurrentHashMap<>();

    public void verifierAutorisation(String email, String adresseIp) {
        String cle = construireCle(email, adresseIp);
        TentativesConnexion tentatives = tentativesParCle.get(cle);
        if (tentatives == null) {
            return;
        }

        synchronized (tentatives) {
            Instant maintenant = Instant.now();
            if (tentatives.bloqueJusqua != null && maintenant.isBefore(tentatives.bloqueJusqua)) {
                throw new TropDeTentativesException("Trop de tentatives de connexion. Reessayez plus tard.");
            }
            if (tentatives.dernierEchec == null || Duration.between(tentatives.dernierEchec, maintenant).compareTo(FENETRE) > 0) {
                tentativesParCle.remove(cle);
            }
        }
    }

    public void enregistrerEchec(String email, String adresseIp) {
        String cle = construireCle(email, adresseIp);
        TentativesConnexion tentatives = tentativesParCle.computeIfAbsent(cle, ignored -> new TentativesConnexion());

        synchronized (tentatives) {
            Instant maintenant = Instant.now();
            if (tentatives.dernierEchec == null || Duration.between(tentatives.dernierEchec, maintenant).compareTo(FENETRE) > 0) {
                tentatives.compteur = 0;
                tentatives.bloqueJusqua = null;
            }

            tentatives.compteur++;
            tentatives.dernierEchec = maintenant;
            if (tentatives.compteur >= MAX_TENTATIVES) {
                tentatives.bloqueJusqua = maintenant.plus(BLOCAGE);
            }
        }
    }

    public void reinitialiser(String email, String adresseIp) {
        tentativesParCle.remove(construireCle(email, adresseIp));
    }

    private String construireCle(String email, String adresseIp) {
        String emailNormalise = StringUtils.hasText(email) ? email.trim().toLowerCase(Locale.ROOT) : "inconnu";
        String ipNormalisee = StringUtils.hasText(adresseIp) ? adresseIp.trim() : "inconnue";
        return emailNormalise + "|" + ipNormalisee;
    }

    private static final class TentativesConnexion {
        private int compteur;
        private Instant dernierEchec;
        private Instant bloqueJusqua;
    }
}
