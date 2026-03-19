package com.mycampus.backend.web.etudiant;

import com.mycampus.backend.dto.annonces.AnnonceSortieEtudiantDTO;
import com.mycampus.backend.dto.annonces.RessourceAnnonceSortieDTO;
import com.mycampus.backend.dto.annonces.TelechargementRessourceAnnonceSortieDTO;
import com.mycampus.backend.metier.annonces.ressources.GestionRessourcesAnnonceMetier;
import com.mycampus.backend.metier.etudiant.annonces.GestionAnnoncesEtudiantMetier;
import com.mycampus.backend.metier.professeur.StockageFichiersService;
import com.mycampus.backend.metier.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etudiant/annonces")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestion des Annonces - Etudiant", description = "API pour la consultation des annonces par les etudiants")
@PreAuthorize("hasAuthority('ETUDIANT_ANNONCES_LIRE')")
public class AnnoncesEtudiantController {

    private final GestionAnnoncesEtudiantMetier gestionAnnoncesEtudiantMetier;
    private final GestionRessourcesAnnonceMetier gestionRessourcesAnnonceMetier;
    private final StockageFichiersService stockageFichiersService;

    @GetMapping
    @Operation(summary = "Lister les annonces visibles", description = "Recupere la liste des annonces visibles pour l'etudiant connecte.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Annonces recuperees avec succes"),
            @ApiResponse(responseCode = "403", description = "Acces non autorise"),
            @ApiResponse(responseCode = "404", description = "Etudiant non trouve")
    })
    public ResponseEntity<List<AnnonceSortieEtudiantDTO>> listerAnnoncesVisibles(
            @AuthenticationPrincipal CustomUserDetails user) {
        List<AnnonceSortieEtudiantDTO> result = gestionAnnoncesEtudiantMetier.listerAnnoncesVisibles(user.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Trouver une annonce par ID", description = "Recupere une annonce specifique si elle est visible pour l'etudiant connecte.")
    public ResponseEntity<AnnonceSortieEtudiantDTO> trouverAnnonceParId(
            @Parameter(description = "ID de l'annonce") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        AnnonceSortieEtudiantDTO result = gestionAnnoncesEtudiantMetier.trouverAnnonceParId(id, user.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/ressources")
    @Operation(summary = "Lister les ressources attachees", description = "Liste les ressources attachees d'une annonce visible par l'etudiant.")
    public ResponseEntity<List<RessourceAnnonceSortieDTO>> listerRessourcesAnnonce(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(gestionRessourcesAnnonceMetier.listerRessourcesEtudiant(id, user.getId()));
    }

    @GetMapping("/ressources/{ressourceId}/telecharger")
    @Operation(summary = "Telecharger une ressource attachee", description = "Telecharge une ressource attachee d'une annonce visible par l'etudiant.")
    public ResponseEntity<Resource> telechargerRessourceAnnonce(
            @PathVariable Long ressourceId,
            @AuthenticationPrincipal CustomUserDetails user) {

        TelechargementRessourceAnnonceSortieDTO telechargement =
                gestionRessourcesAnnonceMetier.preparerTelechargementEtudiant(ressourceId, user.getId());
        Resource ressource = stockageFichiersService.chargerCommeResource(telechargement.getChemin());

        MediaType type = MediaType.APPLICATION_OCTET_STREAM;
        if (telechargement.getTypeMime() != null && !telechargement.getTypeMime().isBlank()) {
            type = MediaType.parseMediaType(telechargement.getTypeMime());
        }

        String nomFichier = telechargement.getNomOriginal() != null ? telechargement.getNomOriginal() : "fichier";
        nomFichier = nomFichier.replace("\"", "");

        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomFichier + "\"")
                .body(ressource);
    }
}
