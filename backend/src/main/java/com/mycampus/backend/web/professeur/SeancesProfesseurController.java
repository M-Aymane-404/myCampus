package com.mycampus.backend.web.professeur;



import com.mycampus.backend.dto.professeur.DonneeEntrer.fichiersSeance.DepotFichierSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.CreationSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.ModificationSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.RechercheSeancesEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.fichiersSeance.FichierSeanceSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.seances.SeanceResumeSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.seances.SeanceSortieDTO;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.metier.professeur.seances.GestionSeancesProfesseurMetier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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

/*
 This controller exposes REST endpoints for professeur seances and files.
 It reads professeurPpr from header to allow easy API testing.
*/
@RestController
@RequestMapping("/api/professeur")
@RequiredArgsConstructor
public class SeancesProfesseurController {

    private final GestionSeancesProfesseurMetier gestionSeancesProfesseurMetier;


    @PostMapping("/seances")
    public SeanceSortieDTO creerSeance(
            @RequestHeader("ProfesseurPpr") String professeurPpr,
            @RequestBody CreationSeanceEntrerDTO creationSeanceEntrerDTO
    ) throws SeancesProfesseurException {
        return gestionSeancesProfesseurMetier.creerSeance(professeurPpr, creationSeanceEntrerDTO);
    }


    @PutMapping("/seances/{seanceId}")
    public SeanceSortieDTO modifierSeance(
            @RequestHeader("ProfesseurPpr") String professeurPpr,
            @PathVariable Long seanceId,
            @RequestBody ModificationSeanceEntrerDTO modificationSeanceEntrerDTO
    ) throws SeancesProfesseurException {
        return gestionSeancesProfesseurMetier.modifierSeance(professeurPpr, seanceId, modificationSeanceEntrerDTO);
    }


    @DeleteMapping("/seances/{seanceId}")
    public void supprimerSeance(
            @RequestHeader("ProfesseurPpr") String professeurPpr,
            @PathVariable Long seanceId
    ) throws SeancesProfesseurException {
        gestionSeancesProfesseurMetier.supprimerSeance(professeurPpr, seanceId);
    }


    @GetMapping("/seances/{seanceId}")
    public SeanceSortieDTO trouverSeanceParId(
            @RequestHeader("ProfesseurPpr") String professeurPpr,
            @PathVariable Long seanceId
    ) throws SeancesProfesseurException {
        return gestionSeancesProfesseurMetier.trouverSeanceParId(professeurPpr, seanceId);
    }

    /*
     This endpoint searches seances by filters (date range, type, etat, affectation).
    */
    @PostMapping("/seances/recherche")
    public List<SeanceResumeSortieDTO> rechercherSeances(
            @RequestHeader("ProfesseurPpr") String professeurPpr,
            @RequestBody RechercheSeancesEntrerDTO rechercheSeancesEntrerDTO
    ) throws SeancesProfesseurException {
        return gestionSeancesProfesseurMetier.rechercherSeances(professeurPpr, rechercheSeancesEntrerDTO);
    }

    /*
     This endpoint uploads one file to a seance (multipart).
     The seanceId is taken from URL, and passed into Depot DTO.
    */
    @PostMapping(value = "/seances/{seanceId}/fichiers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FichierSeanceSortieDTO deposerFichierPourSeance(
            @RequestHeader("ProfesseurPpr") String professeurPpr,
            @PathVariable Long seanceId,
            @RequestPart("fichier") MultipartFile fichier
    ) throws SeancesProfesseurException {

        DepotFichierSeanceEntrerDTO depot = new DepotFichierSeanceEntrerDTO();
        depot.setSeanceId(seanceId);

        return gestionSeancesProfesseurMetier.deposerFichierPourSeance(professeurPpr, depot, fichier);
    }

    /*
     This endpoint lists all files of a seance for the professor (PPR).
    */
    @GetMapping("/seances/{seanceId}/fichiers")
    public List<FichierSeanceSortieDTO> listerFichiersDeSeance(
            @RequestHeader("ProfesseurPpr") String professeurPpr,
            @PathVariable Long seanceId
    ) throws SeancesProfesseurException {
        return gestionSeancesProfesseurMetier.listerFichiersDeSeance(professeurPpr, seanceId);
    }

    /*
     This endpoint deletes one file from a seance (optional feature).
    */
    @DeleteMapping("/fichiers-seance/{fichierId}")
    public void supprimerFichierDeSeance(
            @RequestHeader("ProfesseurPpr") String professeurPpr,
            @PathVariable Long fichierId
    ) throws SeancesProfesseurException {
        gestionSeancesProfesseurMetier.supprimerFichierDeSeance(professeurPpr, fichierId);
    }

    // telechargement fichier
    @GetMapping("/fichiers-seance/{fichierId}/telecharger")
    public ResponseEntity<Resource> telechargerFichierSeance(
            @RequestHeader("ProfesseurPpr") String professeurPpr,
            @PathVariable Long fichierId
    ) throws SeancesProfesseurException {

        TelechargementFichierSeanceSortieDTO telechargement =
                gestionSeancesProfesseurMetier.preparerTelechargementFichierSeance(professeurPpr, fichierId);

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

