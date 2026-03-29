package com.mycampus.backend.web.professeur;

import com.mycampus.backend.dto.professeur.DonneeSortie.presences.JustificatifProfesseurSortieDTO;
import com.mycampus.backend.enumerations.StatutJustificatif;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.metier.professeur.presences.GestionPresencesProfesseurMetier;
import com.mycampus.backend.metier.security.CustomUserDetails;
import com.mycampus.backend.metier.security.GestionUtilisateurConnecteMetier;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/professeur/justificatifs")
@RequiredArgsConstructor
public class JustificatifsProfesseurController {

    private final GestionPresencesProfesseurMetier presenceMetier;
    private final GestionUtilisateurConnecteMetier utilisateurMetier;

    @GetMapping
    @PreAuthorize("hasAuthority('PROFESSEUR_PRESENCES_LIRE')")
    public ResponseEntity<List<JustificatifProfesseurSortieDTO>> listerJustificatifs(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(value = "statut", required = false) String statut,
            @RequestParam(value = "codeClasse", required = false) String codeClasse,
            @RequestParam(value = "recherche", required = false) String recherche,
            @RequestParam(value = "periode", required = false) String periode,
            @RequestParam(value = "dateDebut", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(value = "dateFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin
    ) throws SeancesProfesseurException {

        String professeurPpr = utilisateurMetier.trouverProfesseurPprParId(user.getId());
        LocalDate[] intervalle = resolveDateRange(periode, dateDebut, dateFin);

        return ResponseEntity.ok(
                presenceMetier.listerJustificatifsProfesseur(
                        professeurPpr,
                        parseStatut(statut),
                        normalizeEmpty(codeClasse),
                        normalizeEmpty(recherche),
                        intervalle[0],
                        intervalle[1]
                )
        );
    }

    private StatutJustificatif parseStatut(String statut) throws SeancesProfesseurException {
        if (!StringUtils.hasText(statut) || "TOUS".equalsIgnoreCase(statut)) {
            return null;
        }

        try {
            return StatutJustificatif.valueOf(statut.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new SeancesProfesseurException("Statut de justificatif invalide.");
        }
    }

    private String normalizeEmpty(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private LocalDate[] resolveDateRange(String periode, LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut != null || dateFin != null) {
            return new LocalDate[]{dateDebut, dateFin};
        }

        if (!StringUtils.hasText(periode) || "ALL".equalsIgnoreCase(periode)) {
            return new LocalDate[]{null, null};
        }

        LocalDate now = LocalDate.now();
        return switch (periode.trim().toUpperCase()) {
            case "7D" -> new LocalDate[]{now.minusDays(7), now};
            case "30D" -> new LocalDate[]{now.minusDays(30), now};
            case "THIS_MONTH" -> {
                YearMonth yearMonth = YearMonth.from(now);
                yield new LocalDate[]{yearMonth.atDay(1), yearMonth.atEndOfMonth()};
            }
            case "THIS_YEAR" -> new LocalDate[]{now.withDayOfYear(1), now.withMonth(12).withDayOfMonth(31)};
            default -> new LocalDate[]{null, null};
        };
    }
}
