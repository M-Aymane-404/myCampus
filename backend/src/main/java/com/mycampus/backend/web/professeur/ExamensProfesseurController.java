package com.mycampus.backend.web.professeur;

import com.mycampus.backend.dto.professeur.DonneeEntrer.examens.ExamenCreationEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.examens.ExamenModificationEntrerDTO;

import com.mycampus.backend.dto.professeur.DonneeSortie.examens.ExamenSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.examens.FiltreExamensSortieDTO;
import com.mycampus.backend.metier.professeur.examens.GestionExamensProfesseurMetier;
import com.mycampus.backend.metier.security.CustomUserDetails;
import com.mycampus.backend.metier.security.GestionUtilisateurConnecteMetier;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professeur/examens")
@RequiredArgsConstructor

public class ExamensProfesseurController {

    private final GestionExamensProfesseurMetier examensMetier;
    private final GestionUtilisateurConnecteMetier utilisateurMetier;

    private String ppr(CustomUserDetails principal) {
        return utilisateurMetier.trouverProfesseurPprParId(principal.getId());
    }

    @GetMapping("/filtres")
    @PreAuthorize("hasAuthority('PROFESSEUR_EXAMENS_GERER')")
    public FiltreExamensSortieDTO filtres(@AuthenticationPrincipal CustomUserDetails principal) {
        return examensMetier.chargerFiltresExamens(ppr(principal));
    }
    @PostMapping("/classes/{codeClasse}/matieres/{codeMatiere}")
    @PreAuthorize("hasAuthority('PROFESSEUR_EXAMENS_GERER')")
    public ExamenSortieDTO creer(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String codeClasse,
            @PathVariable String codeMatiere,
            @RequestBody ExamenCreationEntrerDTO dto
    ) {
        return examensMetier.creerExamen(ppr(user), codeClasse, codeMatiere, dto);
    }

    @PutMapping("/{examenId}")
    @PreAuthorize("hasAuthority('PROFESSEUR_EXAMENS_GERER')")
    public ExamenSortieDTO modifier(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long examenId,
            @RequestBody ExamenModificationEntrerDTO dto
    ) {
        return examensMetier.modifierExamen(ppr(user), examenId, dto);
    }

    @DeleteMapping("/{examenId}")
    @PreAuthorize("hasAuthority('PROFESSEUR_EXAMENS_GERER')")
    public void supprimer(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long examenId
    ) {
        examensMetier.supprimerExamen(ppr(user), examenId);
    }





}
