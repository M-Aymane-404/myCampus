package com.mycampus.backend.web.professeur;



import com.mycampus.backend.dto.professeur.DonneeSortie.tableauDeBord.TableauDeBordProfesseurSortieDTO;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.metier.professeur.tableauDeBord.TableauDeBordProfesseurMetier;
import com.mycampus.backend.metier.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/professeur")
@RequiredArgsConstructor
@Tag(name = "Tableau de Bord - Professeur", description = "API pour le tableau de bord professeur")
@PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_LIRE')")
public class TableauDeBordProfesseurController {

    private final TableauDeBordProfesseurMetier tableauDeBordMetier;

    @Operation(summary = "Tableau de bord du professeur connecté")
    @GetMapping("/tableau-de-bord")
    public ResponseEntity<TableauDeBordProfesseurSortieDTO> obtenirTableauDeBord(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        if (user == null) {
            throw new UtilisateursException("Utilisateur non authentifié.");
        }
        TableauDeBordProfesseurSortieDTO dto = tableauDeBordMetier.obtenirTableauDeBord(user.getId());
        return ResponseEntity.ok(dto);
    }
}
