package com.mycampus.backend.web.professeur;



import com.mycampus.backend.dto.professeur.DonneeEntrer.fichiersSeance.DepotFichierSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.CreationSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.ModificationSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.RechercheSeancesEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.fichiersSeance.FichierSeanceSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.seances.SeanceResumeSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.seances.SeanceSortieDTO;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.metier.professeur.seances.GestionSeancesProfesseurMetier;
import com.mycampus.backend.repositorys.ProfesseurRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mycampus.backend.dto.professeur.DonneeSortie.fichiersSeance.TelechargementFichierSeanceSortieDTO;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/professeur")
@RequiredArgsConstructor
public class SeancesProfesseurController {

    private final GestionSeancesProfesseurMetier gestionSeancesProfesseurMetier;
    private final ProfesseurRepository professeurRepository;

    private String requireProfesseurPpr(Jwt jwt) throws SeancesProfesseurException {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new SeancesProfesseurException("JWT invalide: email introuvable.");
        }
        String email = jwt.getSubject();

        Professeur prof = professeurRepository.findByEmail(email)
                .orElseThrow(() -> new SeancesProfesseurException("Professeur introuvable pour cet email."));

        if (prof.getPpr() == null || prof.getPpr().isBlank()) {
            throw new SeancesProfesseurException("PPR introuvable pour ce professeur.");
        }
        return prof.getPpr();
    }

    @PostMapping("/seances")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')") // ou une permission plus “write” si tu la crées
    public SeanceSortieDTO creerSeance(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreationSeanceEntrerDTO creationSeanceEntrerDTO
    ) throws SeancesProfesseurException {
        String ppr = requireProfesseurPpr(jwt);
        return gestionSeancesProfesseurMetier.creerSeance(ppr, creationSeanceEntrerDTO);
    }

    @PutMapping("/seances/{seanceId}")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')")
    public SeanceSortieDTO modifierSeance(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long seanceId,
            @RequestBody ModificationSeanceEntrerDTO modificationSeanceEntrerDTO
    ) throws SeancesProfesseurException {
        String ppr = requireProfesseurPpr(jwt);
        return gestionSeancesProfesseurMetier.modifierSeance(ppr, seanceId, modificationSeanceEntrerDTO);
    }

    @DeleteMapping("/seances/{seanceId}")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')")
    public void supprimerSeance(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long seanceId
    ) throws SeancesProfesseurException {
        String ppr = requireProfesseurPpr(jwt);
        gestionSeancesProfesseurMetier.supprimerSeance(ppr, seanceId);
    }

    @GetMapping("/seances/{seanceId}")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')")
    public SeanceSortieDTO trouverSeanceParId(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long seanceId
    ) throws SeancesProfesseurException {
        String ppr = requireProfesseurPpr(jwt);
        return gestionSeancesProfesseurMetier.trouverSeanceParId(ppr, seanceId);
    }

    @PostMapping("/seances/recherche")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')")
    public List<SeanceResumeSortieDTO> rechercherSeances(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody RechercheSeancesEntrerDTO rechercheSeancesEntrerDTO
    ) throws SeancesProfesseurException {
        String ppr = requireProfesseurPpr(jwt);
        return gestionSeancesProfesseurMetier.rechercherSeances(ppr, rechercheSeancesEntrerDTO);
    }

    @PostMapping(value = "/seances/{seanceId}/fichiers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')")
    public FichierSeanceSortieDTO deposerFichierPourSeance(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long seanceId,
            @RequestPart("fichier") MultipartFile fichier
    ) throws SeancesProfesseurException {
        String ppr = requireProfesseurPpr(jwt);

        DepotFichierSeanceEntrerDTO depot = new DepotFichierSeanceEntrerDTO();
        depot.setSeanceId(seanceId);

        return gestionSeancesProfesseurMetier.deposerFichierPourSeance(ppr, depot, fichier);
    }

    @GetMapping("/seances/{seanceId}/fichiers")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')")
    public List<FichierSeanceSortieDTO> listerFichiersDeSeance(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long seanceId
    ) throws SeancesProfesseurException {
        String ppr = requireProfesseurPpr(jwt);
        return gestionSeancesProfesseurMetier.listerFichiersDeSeance(ppr, seanceId);
    }

    @DeleteMapping("/fichiers-seance/{fichierId}")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')")
    public void supprimerFichierDeSeance(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long fichierId
    ) throws SeancesProfesseurException {
        String ppr = requireProfesseurPpr(jwt);
        gestionSeancesProfesseurMetier.supprimerFichierDeSeance(ppr, fichierId);
    }

    @GetMapping("/fichiers-seance/{fichierId}/telecharger")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')")
    public ResponseEntity<Resource> telechargerFichierSeance(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long fichierId
    ) throws SeancesProfesseurException {

        String ppr = requireProfesseurPpr(jwt);
        TelechargementFichierSeanceSortieDTO telechargement =
                gestionSeancesProfesseurMetier.preparerTelechargementFichierSeance(ppr, fichierId);

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
            throw new SeancesProfesseurException("Chemin du fichier invalide.");
        }
      }


    }










