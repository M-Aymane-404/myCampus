package com.mycampus.backend.web.professeur.annonces;

import com.mycampus.backend.dto.annonces.AnnonceEntrerDTO;
import com.mycampus.backend.dto.annonces.AnnonceSortieDTO;
import com.mycampus.backend.metier.professeur.annonces.GestionAnnoncesProfesseurMetier;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professeur/annonces")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestion des Annonces - Professeur", description = "API pour la gestion des annonces par les professeurs")
@PreAuthorize("hasAuthority('PROFESSEUR_NOTES_GERER')")
public class ProfesseurAnnonceController {

    private final GestionAnnoncesProfesseurMetier gestionAnnoncesProfesseurMetier;

    @PostMapping
    @Operation(summary = "Créer une annonce", description = "Crée une nouvelle annonce pour une classe assignée au professeur. Le professeur ne peut pas créer d'annonces globales.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Annonce créée avec succès",
            content = @Content(schema = @Schema(implementation = AnnonceSortieDTO.class))),
        @ApiResponse(responseCode = "400", description = "Données invalides ou classe non assignée"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<AnnonceSortieDTO> creerAnnonce(
            @Valid @RequestBody AnnonceEntrerDTO annonceDTO,
            Authentication authentication) {
        
        String emailAuteur = authentication.getName();
        log.info("Création d'annonce par le professeur: {}", emailAuteur);
        AnnonceSortieDTO result = gestionAnnoncesProfesseurMetier.creerAnnonce(annonceDTO, emailAuteur);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/mes-classes")
    @Operation(summary = "Lister les annonces de ses classes et globales", description = "Récupère la liste des annonces (globales et de classe) pour les classes assignées au professeur.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Annonces des classes récupérées avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<List<AnnonceSortieDTO>> listerAnnoncesDeSesClasses(
            Authentication authentication) {
        
        String emailProfesseur = authentication.getName();
        log.info("Listage des annonces des classes du professeur: {}", emailProfesseur);
        List<AnnonceSortieDTO> result = gestionAnnoncesProfesseurMetier.listerAnnoncesDeSesClasses(emailProfesseur);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/mes-annonces")
    @Operation(summary = "Lister mes annonces", description = "Récupère la liste des annonces créées par le professeur.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Annonces du professeur récupérées avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<List<AnnonceSortieDTO>> listerMesAnnonces(
            Authentication authentication) {
        
        String emailProfesseur = authentication.getName();
        log.info("Listage des annonces créées par le professeur: {}", emailProfesseur);
        List<AnnonceSortieDTO> result = gestionAnnoncesProfesseurMetier.listerMesAnnonces(emailProfesseur);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{annonceId}")
    @Operation(summary = "Supprimer mon annonce", description = "Supprime une annonce créée par le professeur.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Annonce supprimée avec succès"),
        @ApiResponse(responseCode = "404", description = "Annonce non trouvée"),
        @ApiResponse(responseCode = "403", description = "Annonce non appartenant au professeur")
    })
    public ResponseEntity<Void> supprimerMonAnnonce(
            @Parameter(description = "ID de l'annonce à supprimer") @PathVariable Long annonceId,
            Authentication authentication) {
        
        String emailProfesseur = authentication.getName();
        log.info("Suppression de l'annonce: {} par le professeur: {}", annonceId, emailProfesseur);
        gestionAnnoncesProfesseurMetier.supprimerMonAnnonce(annonceId, emailProfesseur);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{annonceId}/publier")
    @Operation(summary = "Publier mon annonce", description = "Marque une annonce du professeur comme publiée.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Annonce publiée avec succès"),
        @ApiResponse(responseCode = "404", description = "Annonce non trouvée"),
        @ApiResponse(responseCode = "403", description = "Annonce non appartenant au professeur")
    })
    public ResponseEntity<AnnonceSortieDTO> publierMonAnnonce(
            @Parameter(description = "ID de l'annonce à publier") @PathVariable Long annonceId,
            Authentication authentication) {
        
        String emailProfesseur = authentication.getName();
        log.info("Publication de l'annonce: {} par le professeur: {}", annonceId, emailProfesseur);
        AnnonceSortieDTO result = gestionAnnoncesProfesseurMetier.publierMonAnnonce(annonceId, emailProfesseur);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{annonceId}/depublier")
    @Operation(summary = "Dépublier mon annonce", description = "Marque une annonce du professeur comme non publiée.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Annonce dépubliée avec succès"),
        @ApiResponse(responseCode = "404", description = "Annonce non trouvée"),
        @ApiResponse(responseCode = "403", description = "Annonce non appartenant au professeur")
    })
    public ResponseEntity<AnnonceSortieDTO> depublierMonAnnonce(
            @Parameter(description = "ID de l'annonce à dépublier") @PathVariable Long annonceId,
            Authentication authentication) {
        
        String emailProfesseur = authentication.getName();
        log.info("Dépublication de l'annonce: {} par le professeur: {}", annonceId, emailProfesseur);
        AnnonceSortieDTO result = gestionAnnoncesProfesseurMetier.depublierMonAnnonce(annonceId, emailProfesseur);
        return ResponseEntity.ok(result);
    }
}
