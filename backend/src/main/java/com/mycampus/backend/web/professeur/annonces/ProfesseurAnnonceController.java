package com.mycampus.backend.web.professeur.annonces;

import com.mycampus.backend.dto.annonces.AnnonceEntrerDTO;
import com.mycampus.backend.dto.annonces.AnnonceSortieDTO;
import com.mycampus.backend.dto.annonces.RessourceAnnonceSortieDTO;
import com.mycampus.backend.dto.annonces.TelechargementRessourceAnnonceSortieDTO;
import com.mycampus.backend.metier.annonces.ressources.GestionRessourcesAnnonceMetier;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/professeur/annonces")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestion des Annonces - Professeur", description = "API pour la gestion des annonces par les professeurs")
@PreAuthorize("hasAuthority('PROFESSEUR_ANNONCES_GERER')")
public class ProfesseurAnnonceController {

    private final GestionAnnoncesProfesseurMetier gestionAnnoncesProfesseurMetier;
    private final GestionRessourcesAnnonceMetier gestionRessourcesAnnonceMetier;

    @PostMapping
    @Operation(summary = "Creer une annonce", description = "Cree une nouvelle annonce pour une classe assignee au professeur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Annonce creee avec succes",
                    content = @Content(schema = @Schema(implementation = AnnonceSortieDTO.class))),
            @ApiResponse(responseCode = "400", description = "Donnees invalides ou classe non assignee"),
            @ApiResponse(responseCode = "403", description = "Acces non autorise")
    })
    public ResponseEntity<AnnonceSortieDTO> creerAnnonce(
            @Valid @RequestBody AnnonceEntrerDTO annonceDTO,
            Authentication authentication) {

        String emailAuteur = authentication.getName();
        log.info("Creation d'annonce par le professeur: {}", emailAuteur);
        AnnonceSortieDTO result = gestionAnnoncesProfesseurMetier.creerAnnonce(annonceDTO, emailAuteur);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/mes-classes")
    @Operation(summary = "Lister les annonces de ses classes et globales", description = "Recupere la liste des annonces pour les classes assignees au professeur.")
    public ResponseEntity<List<AnnonceSortieDTO>> listerAnnoncesDeSesClasses(Authentication authentication) {
        String emailProfesseur = authentication.getName();
        List<AnnonceSortieDTO> result = gestionAnnoncesProfesseurMetier.listerAnnoncesDeSesClasses(emailProfesseur);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/mes-annonces")
    @Operation(summary = "Lister mes annonces", description = "Recupere la liste des annonces creees par le professeur.")
    public ResponseEntity<List<AnnonceSortieDTO>> listerMesAnnonces(Authentication authentication) {
        String emailProfesseur = authentication.getName();
        List<AnnonceSortieDTO> result = gestionAnnoncesProfesseurMetier.listerMesAnnonces(emailProfesseur);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/mes-prof-annonces")
    @Operation(summary = "Lister mes departement/global annonces", description = "Recupere les annonces de mon departement et les globales.")
    public ResponseEntity<List<AnnonceSortieDTO>> listerMesDepEtGlobalAnnonces(Authentication authentication) {
        String emailProfesseur = authentication.getName();
        List<AnnonceSortieDTO> result = gestionAnnoncesProfesseurMetier.listerLesAnnoncesDeMonDepEtGlobal(emailProfesseur);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{annonceId}")
    @Operation(summary = "Supprimer mon annonce", description = "Supprime une annonce creee par le professeur.")
    public ResponseEntity<Void> supprimerMonAnnonce(
            @Parameter(description = "ID de l'annonce a supprimer") @PathVariable Long annonceId,
            Authentication authentication) {

        String emailProfesseur = authentication.getName();
        gestionAnnoncesProfesseurMetier.supprimerMonAnnonce(annonceId, emailProfesseur);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{annonceId}/publier")
    @Operation(summary = "Publier mon annonce", description = "Marque une annonce du professeur comme publiee.")
    public ResponseEntity<AnnonceSortieDTO> publierMonAnnonce(
            @PathVariable Long annonceId,
            Authentication authentication) {

        String emailProfesseur = authentication.getName();
        AnnonceSortieDTO result = gestionAnnoncesProfesseurMetier.publierMonAnnonce(annonceId, emailProfesseur);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{annonceId}/depublier")
    @Operation(summary = "Depublier mon annonce", description = "Marque une annonce du professeur comme non publiee.")
    public ResponseEntity<AnnonceSortieDTO> depublierMonAnnonce(
            @PathVariable Long annonceId,
            Authentication authentication) {

        String emailProfesseur = authentication.getName();
        AnnonceSortieDTO result = gestionAnnoncesProfesseurMetier.depublierMonAnnonce(annonceId, emailProfesseur);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/{annonceId}/ressources", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Ajouter une ressource attachee", description = "Ajoute une ressource attachee a une annonce du professeur.")
    public ResponseEntity<RessourceAnnonceSortieDTO> ajouterRessource(
            @PathVariable Long annonceId,
            @RequestPart("fichier") MultipartFile fichier,
            Authentication authentication) {

        RessourceAnnonceSortieDTO result = gestionRessourcesAnnonceMetier.ajouterRessourceProfesseur(
                annonceId,
                authentication.getName(),
                fichier
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{annonceId}/ressources")
    @Operation(summary = "Lister les ressources attachees", description = "Liste les ressources attachees d'une annonce du professeur.")
    public ResponseEntity<List<RessourceAnnonceSortieDTO>> listerRessources(
            @PathVariable Long annonceId,
            Authentication authentication) {

        return ResponseEntity.ok(
                gestionRessourcesAnnonceMetier.listerRessourcesProfesseur(annonceId, authentication.getName())
        );
    }

    @GetMapping("/ressources/{ressourceId}/telecharger")
    @Operation(summary = "Telecharger une ressource attachee", description = "Telecharge une ressource attachee d'annonce du professeur.")
    public ResponseEntity<Resource> telechargerRessource(
            @PathVariable Long ressourceId,
            Authentication authentication) {

        TelechargementRessourceAnnonceSortieDTO telechargement =
                gestionRessourcesAnnonceMetier.preparerTelechargementProfesseur(
                        ressourceId,
                        authentication.getName()
                );

        try {
            Path chemin = Paths.get(telechargement.getChemin()).toAbsolutePath().normalize();
            Resource ressource = new UrlResource(chemin.toUri());

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
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Chemin du fichier invalide.");
        }
    }
}
