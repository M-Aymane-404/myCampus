package com.mycampus.backend.web.professeur;



import com.mycampus.backend.dto.administrateur.DonneSortie.AffectationEnseignementSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.fichiersSeance.DepotFichierSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.CreationSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.ModificationSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.RechercheSeancesEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.fichiersSeance.FichierSeanceSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.seances.SeanceResumeSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.seances.SeanceSortieDTO;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.metier.professeur.StockageFichiersService;
import com.mycampus.backend.metier.professeur.seances.GestionSeancesProfesseurMetier;
import com.mycampus.backend.metier.security.CustomUserDetails;
import com.mycampus.backend.metier.security.GestionUtilisateurConnecteMetier;
import com.mycampus.backend.repositorys.ProfesseurRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mycampus.backend.dto.professeur.DonneeSortie.fichiersSeance.TelechargementFichierSeanceSortieDTO;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/professeur")
@RequiredArgsConstructor
public class SeancesProfesseurController {

    private final GestionSeancesProfesseurMetier gestionSeancesProfMetier;
   // private final ProfesseurRepository professeurRepository;
    private final GestionUtilisateurConnecteMetier utilisateurMetier;
    private final StockageFichiersService stockageFichiersService;



    @GetMapping("/enseignements")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')")
    public List<AffectationEnseignementSortieDTO> listerEnseignements(
            @AuthenticationPrincipal CustomUserDetails user
            ) throws SeancesProfesseurException , UtilisateursException {
        String ppr = utilisateurMetier.trouverProfesseurPprParId(user.getId());
        return gestionSeancesProfMetier.listerEnseignements(ppr);
    }

    @PostMapping("/seances")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')") // ou une permission plus “write” si tu la crées
    public SeanceSortieDTO creerSeance(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody CreationSeanceEntrerDTO creationSeanceEntrerDTO
    ) throws SeancesProfesseurException {

        String ppr = utilisateurMetier.trouverProfesseurPprParId(user.getId());
        return gestionSeancesProfMetier.creerSeance(ppr,creationSeanceEntrerDTO);
    }

    @PutMapping("/seances/{seanceId}")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')")
    public SeanceSortieDTO modifierSeance(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long seanceId,
            @RequestBody ModificationSeanceEntrerDTO modificationSeanceEntrerDTO
    ) throws SeancesProfesseurException {
        String ppr = utilisateurMetier.trouverProfesseurPprParId(user.getId());
        return gestionSeancesProfMetier.modifierSeance(ppr, seanceId, modificationSeanceEntrerDTO);
    }

    @DeleteMapping("/seances/{seanceId}")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')")
    public void supprimerSeance(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long seanceId
    ) throws SeancesProfesseurException {
        String ppr = utilisateurMetier.trouverProfesseurPprParId(user.getId());
        gestionSeancesProfMetier.supprimerSeance(ppr, seanceId);
    }

    @GetMapping("/seances/{seanceId}")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')")
    public SeanceSortieDTO trouverSeanceParId(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long seanceId
    ) throws SeancesProfesseurException {
        String ppr = utilisateurMetier.trouverProfesseurPprParId(user.getId());
        return gestionSeancesProfMetier.trouverSeanceParId(ppr, seanceId);
    }

    @PostMapping("/seances/recherche")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')")
    public List<SeanceResumeSortieDTO> rechercherSeances(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody RechercheSeancesEntrerDTO rechercheSeancesEntrerDTO
    ) throws SeancesProfesseurException {
        String ppr = utilisateurMetier.trouverProfesseurPprParId(user.getId());
        return gestionSeancesProfMetier.rechercherSeances(ppr, rechercheSeancesEntrerDTO);
    }

    @PostMapping(value = "/seances/{seanceId}/fichiers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')")
    public FichierSeanceSortieDTO deposerFichierPourSeance(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long seanceId,
            @RequestPart("fichier") MultipartFile fichier
    ) throws SeancesProfesseurException {
        String ppr = utilisateurMetier.trouverProfesseurPprParId(user.getId());

        DepotFichierSeanceEntrerDTO depot = new DepotFichierSeanceEntrerDTO();
        depot.setSeanceId(seanceId);

        return gestionSeancesProfMetier.deposerFichierPourSeance(ppr, depot, fichier);
    }

    @GetMapping("/seances/{seanceId}/fichiers")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')")
    public List<FichierSeanceSortieDTO> listerFichiersDeSeance(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long seanceId
    ) throws SeancesProfesseurException {
        String ppr = utilisateurMetier.trouverProfesseurPprParId(user.getId());
        return gestionSeancesProfMetier.listerFichiersDeSeance(ppr, seanceId);
    }

    @DeleteMapping("/fichiers-seance/{fichierId}")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')")
    public void supprimerFichierDeSeance(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long fichierId
    ) throws SeancesProfesseurException {
        String ppr = utilisateurMetier.trouverProfesseurPprParId(user.getId());
        gestionSeancesProfMetier.supprimerFichierDeSeance(ppr, fichierId);
    }

    @GetMapping("/fichiers-seance/{fichierId}/telecharger")
    @PreAuthorize("hasAuthority('PROFESSEUR_SEANCES_GERER')")
    public ResponseEntity<Resource> telechargerFichierSeance(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long fichierId
    ) throws SeancesProfesseurException {

        String ppr = utilisateurMetier.trouverProfesseurPprParId(user.getId());
        TelechargementFichierSeanceSortieDTO telechargement =
                gestionSeancesProfMetier.preparerTelechargementFichierSeance(ppr, fichierId);
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










