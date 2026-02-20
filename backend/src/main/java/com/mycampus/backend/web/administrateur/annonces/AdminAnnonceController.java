package com.mycampus.backend.web.administrateur.annonces;

import com.mycampus.backend.dto.annonces.AnnonceEntrerDTO;
import com.mycampus.backend.dto.annonces.AnnonceSortieDTO;
import com.mycampus.backend.metier.administrateur.annonces.GestionAnnoncesAdministrateurMetier;
import com.mycampus.backend.metier.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/annonces")
@Slf4j
@Tag(name = "Gestion des Annonces - Administrateur", description = "API pour la gestion des annonces par les administrateurs")
@PreAuthorize("hasAuthority('ADMIN_UTILISATEUR_GERER')")
public class AdminAnnonceController {

    private final GestionAnnoncesAdministrateurMetier gestionAnnoncesAdministrateurMetier;

    @PostMapping
    @Operation(summary = "Créer une annonce", description = "Crée une nouvelle annonce. L'administrateur peut créer des annonces globales ou pour n'importe quelle classe.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Annonce créée avec succès",
            content = @Content(schema = @Schema(implementation = AnnonceSortieDTO.class))),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<AnnonceSortieDTO> creerAnnonce(
            @Valid @RequestBody AnnonceEntrerDTO annonceDTO,
            @AuthenticationPrincipal CustomUserDetails user) {
        
         AnnonceSortieDTO result = gestionAnnoncesAdministrateurMetier.creerAnnonce(annonceDTO, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    @Operation(summary = "Lister toutes les annonces", description = "Récupère la liste de toutes les annonces du système.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des annonces récupérée avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<List<AnnonceSortieDTO>> listerToutesAnnonces() {
        log.info("Listage de toutes les annonces demandé par l'administrateur");
        List<AnnonceSortieDTO> result = gestionAnnoncesAdministrateurMetier.listerToutesAnnonces();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/classe/{codeClasse}")
    @Operation(summary = "Lister les annonces d'une classe", description = "Récupère la liste des annonces pour une classe spécifique.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Annonces de la classe récupérées avec succès"),
        @ApiResponse(responseCode = "404", description = "Classe non trouvée"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<List<AnnonceSortieDTO>> listerAnnoncesParClasse(
            @Parameter(description = "Code de la classe") @PathVariable String codeClasse) {
        
        log.info("Listage des annonces pour la classe: {}", codeClasse);
        List<AnnonceSortieDTO> result = gestionAnnoncesAdministrateurMetier.listerAnnoncesParClasse(codeClasse);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{annonceId}")
    @Operation(summary = "Supprimer une annonce", description = "Supprime une annonce du système.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Annonce supprimée avec succès"),
        @ApiResponse(responseCode = "404", description = "Annonce non trouvée"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<Void> supprimerAnnonce(
            @Parameter(description = "ID de l'annonce à supprimer") @PathVariable Long annonceId) {
        
        log.info("Suppression de l'annonce: {}", annonceId);
        gestionAnnoncesAdministrateurMetier.supprimerAnnonce(annonceId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{annonceId}/publier")
    @Operation(summary = "Publier une annonce", description = "Marque une annonce comme publiée.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Annonce publiée avec succès"),
        @ApiResponse(responseCode = "404", description = "Annonce non trouvée"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<AnnonceSortieDTO> publierAnnonce(
            @Parameter(description = "ID de l'annonce à publier") @PathVariable Long annonceId) {
        
        log.info("Publication de l'annonce: {}", annonceId);
        AnnonceSortieDTO result = gestionAnnoncesAdministrateurMetier.publierAnnonce(annonceId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{annonceId}/depublier")
    @Operation(summary = "Dépublier une annonce", description = "Marque une annonce comme non publiée.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Annonce dépubliée avec succès"),
        @ApiResponse(responseCode = "404", description = "Annonce non trouvée"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<AnnonceSortieDTO> depublierAnnonce(
            @Parameter(description = "ID de l'annonce à dépublier") @PathVariable Long annonceId) {
        
        log.info("Dépublication de l'annonce: {}", annonceId);
        AnnonceSortieDTO result = gestionAnnoncesAdministrateurMetier.depublierAnnonce(annonceId);
        return ResponseEntity.ok(result);
    }
}
