package com.mycampus.backend.web.administrateur.annonces;

import com.mycampus.backend.dto.annonces.AnnonceEntrerDTO;
import com.mycampus.backend.dto.annonces.AnnonceSortieDTO;
import com.mycampus.backend.dto.annonces.RessourceAnnonceSortieDTO;
import com.mycampus.backend.dto.annonces.TelechargementRessourceAnnonceSortieDTO;
import com.mycampus.backend.metier.administrateur.annonces.GestionAnnoncesAdministrateurMetier;
import com.mycampus.backend.metier.annonces.ressources.GestionRessourcesAnnonceMetier;
import com.mycampus.backend.metier.professeur.StockageFichiersService;
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
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/annonces")
@Slf4j
@Tag(name = "Gestion des Annonces - Administrateur", description = "API pour la gestion des annonces par les administrateurs")
@PreAuthorize("hasAuthority('ADMIN_UTILISATEUR_GERER')")
public class AdminAnnonceController {

    private final GestionAnnoncesAdministrateurMetier gestionAnnoncesAdministrateurMetier;
    private final GestionRessourcesAnnonceMetier gestionRessourcesAnnonceMetier;
    private final StockageFichiersService stockageFichiersService;

    @PostMapping
    @Operation(summary = "Creer une annonce", description = "Cree une nouvelle annonce. L'administrateur peut creer des annonces globales ou pour n'importe quelle classe.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Annonce creee avec succes",
                    content = @Content(schema = @Schema(implementation = AnnonceSortieDTO.class))),
            @ApiResponse(responseCode = "400", description = "Donnees invalides"),
            @ApiResponse(responseCode = "403", description = "Acces non autorise")
    })
    public ResponseEntity<AnnonceSortieDTO> creerAnnonce(
            @Valid @RequestBody AnnonceEntrerDTO annonceDTO,
            @AuthenticationPrincipal CustomUserDetails user) {

        AnnonceSortieDTO result = gestionAnnoncesAdministrateurMetier.creerAnnonce(annonceDTO, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    @Operation(summary = "Lister toutes les annonces", description = "Recupere la liste de toutes les annonces du systeme.")
    public ResponseEntity<List<AnnonceSortieDTO>> listerToutesAnnonces() {
        log.info("Listage de toutes les annonces demande par l'administrateur");
        List<AnnonceSortieDTO> result = gestionAnnoncesAdministrateurMetier.listerToutesAnnonces();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/classe/{codeClasse}")
    @Operation(summary = "Lister les annonces d'une classe", description = "Recupere la liste des annonces pour une classe specifique.")
    public ResponseEntity<List<AnnonceSortieDTO>> listerAnnoncesParClasse(
            @Parameter(description = "Code de la classe") @PathVariable String codeClasse) {

        log.info("Listage des annonces pour la classe: {}", codeClasse);
        List<AnnonceSortieDTO> result = gestionAnnoncesAdministrateurMetier.listerAnnoncesParClasse(codeClasse);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{annonceId}")
    @Operation(summary = "Supprimer une annonce", description = "Supprime une annonce du systeme.")
    public ResponseEntity<Void> supprimerAnnonce(@PathVariable Long annonceId) {
        log.info("Suppression de l'annonce: {}", annonceId);
        gestionAnnoncesAdministrateurMetier.supprimerAnnonce(annonceId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{annonceId}/publier")
    @Operation(summary = "Publier une annonce", description = "Marque une annonce comme publiee.")
    public ResponseEntity<AnnonceSortieDTO> publierAnnonce(@PathVariable Long annonceId) {
        log.info("Publication de l'annonce: {}", annonceId);
        AnnonceSortieDTO result = gestionAnnoncesAdministrateurMetier.publierAnnonce(annonceId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{annonceId}/depublier")
    @Operation(summary = "Depublier une annonce", description = "Marque une annonce comme non publiee.")
    public ResponseEntity<AnnonceSortieDTO> depublierAnnonce(@PathVariable Long annonceId) {
        log.info("Depublication de l'annonce: {}", annonceId);
        AnnonceSortieDTO result = gestionAnnoncesAdministrateurMetier.depublierAnnonce(annonceId);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/{annonceId}/ressources", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Ajouter des ressources attachees", description = "Ajoute une ou plusieurs ressources attachees a une annonce.")
    public ResponseEntity<List<RessourceAnnonceSortieDTO>> ajouterRessources(
            @PathVariable Long annonceId,
            @RequestPart(value = "fichiers", required = false) List<MultipartFile> fichiers,
            @RequestPart(value = "fichier", required = false) MultipartFile fichier) {

        List<RessourceAnnonceSortieDTO> result = gestionRessourcesAnnonceMetier.ajouterRessourcesAdmin(
                annonceId,
                normaliserFichiers(fichiers, fichier)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{annonceId}/ressources")
    @Operation(summary = "Lister les ressources attachees", description = "Liste les ressources attachees d'une annonce.")
    public ResponseEntity<List<RessourceAnnonceSortieDTO>> listerRessources(@PathVariable Long annonceId) {
        return ResponseEntity.ok(gestionRessourcesAnnonceMetier.listerRessourcesAdmin(annonceId));
    }

    @GetMapping("/ressources/{ressourceId}/telecharger")
    @Operation(summary = "Telecharger une ressource attachee", description = "Telecharge une ressource attachee d'annonce.")
    public ResponseEntity<Resource> telechargerRessource(@PathVariable Long ressourceId) {
        TelechargementRessourceAnnonceSortieDTO telechargement =
                gestionRessourcesAnnonceMetier.preparerTelechargementAdmin(ressourceId);
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

    private List<MultipartFile> normaliserFichiers(List<MultipartFile> fichiers, MultipartFile fichier) {
        if (fichiers != null && !fichiers.isEmpty()) {
            return fichiers;
        }
        if (fichier != null && !fichier.isEmpty()) {
            return List.of(fichier);
        }
        return List.of();
    }
}
