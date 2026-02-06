package com.mycampus.backend.web.etudiant;

import com.mycampus.backend.dto.FichierStocker.FichierStockerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.EtudiantModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.EtudiantSortieDTO;
import com.mycampus.backend.dto.etudiant.DonneeEntrer.EtudiantModificationEtudiantDTO;
import com.mycampus.backend.dto.etudiant.DonneeSortie.*;
import com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote.ReleveDeNoteEtudiantParSessionDTO;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.StatusAbsence;
import com.mycampus.backend.enumerations.TypeSeance;
import com.mycampus.backend.metier.administrateur.utilisateurs.GestionUtilisateursMetier;
import com.mycampus.backend.metier.etudiant.StructureEtudiant.GestionStructureEtudiantMetier;
import com.mycampus.backend.metier.security.GestionUtilisateurConnecteMetier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/etudiant")
@RequiredArgsConstructor
public class StructureAcademiqueEtudiantController {

    private final GestionStructureEtudiantMetier metier;
    private final GestionUtilisateurConnecteMetier utilisateurMetier;
    private final GestionUtilisateursMetier gestionUtilisateursMetier;



    @GetMapping("/moi")
    @Operation(summary = "Trouver un étudiant connecté par son ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Étudiant trouvé"),
            @ApiResponse(responseCode = "404", description = "Étudiant introuvable")
    })
    @PreAuthorize("hasAuthority('ETUDIANT_INFORMATION_PERSONNEL_LIRE')")
    public ResponseEntity<EtudiantSortieEtudiantDTO> trouverEtudiantConnecter(Authentication authentication) {
        Long id = utilisateurMetier.trouverIdUtilisateurConnecte(authentication.getName());
        EtudiantSortieEtudiantDTO result = metier.trouverEtudiantConnecter(id);
        return ResponseEntity.ok(result);
    }


    @PreAuthorize("hasAuthority('ETUDIANT_INFORMATION_PERSONNEL_MODIFIER')")
    @PutMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FichierStockerDTO modifierPhotoProfil(
            Authentication authentication,
            @RequestPart("photo") MultipartFile photo
    ) {
        Utilisateur u = gestionUtilisateursMetier.changerPhotoProfil(authentication.getName(), photo);
        return new FichierStockerDTO(u.getPhotoNom(),u.getPhotoUrl());

    }


    @Operation(summary = "modifier un étudiant connecté ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Étudiant modifier"),
            @ApiResponse(responseCode = "404", description = "Étudiant introuvable")
    })

    @PreAuthorize("hasAuthority('ETUDIANT_INFORMATION_PERSONNEL_MODIFIER')")
    @PutMapping("/moi/modifier")
    public ResponseEntity<EtudiantSortieEtudiantDTO> modifierEtudiant(Authentication authentication, @Valid @RequestBody EtudiantModificationEtudiantDTO etudiantModificationDTO) {
        EtudiantSortieEtudiantDTO etudiant = metier.modifierEtudiant(authentication.getName(), etudiantModificationDTO);
        return ResponseEntity.ok(etudiant);
    }

    @GetMapping("/moi/modules")
    @Operation(summary = "Lister les modules d'un étudiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Modules trouvés"),
            @ApiResponse(responseCode = "404", description = "Étudiant introuvable")
    })
    @PreAuthorize("hasAuthority('ETUDIANT_MODULES_LIRE')")
    public ResponseEntity<List<ModuleSortieEtudiantDTO>> trouverModulesDEtudiantConnecter(
            Authentication authentication,
            @RequestParam(required = false) Semestre semestre) {
        Long id = utilisateurMetier.trouverIdUtilisateurConnecte(authentication.getName());

        List<ModuleSortieEtudiantDTO> result;
        if (semestre != null) {
            result = metier.trouverModulesDEtudiantConnecterParSemester(id, semestre);
        } else {
            result = metier.trouverModulesDEtudiantConnecter(id);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/moi/modules/{codeModule}/matieres")
    @Operation(summary = "Lister les matières d'un module pour un étudiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matières trouvées"),
            @ApiResponse(responseCode = "404", description = "Étudiant ou module introuvable")
    })
    @PreAuthorize("hasAuthority('ETUDIANT_MATIERES_LIRE')")
 
    public ResponseEntity<List<MatiereSortieEtudiantDTO>> trouverMatiereParCodeModule(
            Authentication authentication,
            @PathVariable String codeModule) {
        Long id = utilisateurMetier.trouverIdUtilisateurConnecte(authentication.getName());

        List<MatiereSortieEtudiantDTO> result = metier.trouverMatiereParCodeModule(id, codeModule);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/moi/matieres/{codeMatiere}/seances")
    @Operation(summary = "Lister les séances d'une matière pour un étudiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Séances trouvées"),
            @ApiResponse(responseCode = "404", description = "Étudiant ou matière introuvable")
    })
    @PreAuthorize("hasAuthority('ETUDIANT_SEANCES_LIRE')")

    public ResponseEntity<List<SeanceSortieEtudiantDTO>> trouverSeanceParCodeMatiereEtFiltrerParTypeSeanceSiExist(
            Authentication authentication,
            @PathVariable String codeMatiere,
            @RequestParam(required = false) TypeSeance typeSeance) {
        Long id = utilisateurMetier.trouverIdUtilisateurConnecte(authentication.getName());

        List<SeanceSortieEtudiantDTO> result = metier.trouverSeanceParCodeMatiereEtFiltrerParTypeSeanceSiExist(id, codeMatiere, typeSeance);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/seances/{seanceId}/ressources")
    @Operation(summary = "Lister les ressources d'une séance")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ressources trouvées"),
            @ApiResponse(responseCode = "404", description = "Séance introuvable")
    })
    @PreAuthorize("hasAuthority('ETUDIANT_RESSOURCES_LIRE')")
    public ResponseEntity<List<FichierSeanceSortieEtudiantDTO>> trouverRessourceD_uneSeance(@PathVariable Long seanceId) {
        List<FichierSeanceSortieEtudiantDTO> result = metier.trouverRessourceD_uneSeance(seanceId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("moi/presences")
    @Operation(summary = "Lister les présences d'un étudiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Présences trouvées"),
            @ApiResponse(responseCode = "404", description = "Étudiant introuvable")
    })
    @PreAuthorize("hasAuthority('ETUDIANT_PRESENCES_LIRE')")
    public ResponseEntity<List<PresenceSortieEtudiantDTO>> trouverToutPresenceDuEtudiantConnecter(
            Authentication authentication,
            @RequestParam(required = false) StatusAbsence status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long id = utilisateurMetier.trouverIdUtilisateurConnecte(authentication.getName());

        List<PresenceSortieEtudiantDTO> result = metier.trouverToutPresenceDuEtudiantConnecter(id, status, date);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/moi/releve-notes")
    @Operation(summary = "Obtenir le relevé de notes d'un étudiant par semestre")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relevé de notes trouvé"),
            @ApiResponse(responseCode = "404", description = "Étudiant introuvable"),
            @ApiResponse(responseCode = "400", description = "Paramètre semestre manquant")
    })
    @PreAuthorize("hasAuthority('ETUDIANT_RELEVE_LIRE')")
    public ResponseEntity<ReleveDeNoteEtudiantParSessionDTO> trouverLeReleverDeNoteParSessionDuEtudiant(
            Authentication authentication,
            @RequestParam Semestre semestre) {
        Long id = utilisateurMetier.trouverIdUtilisateurConnecte(authentication.getName());

        ReleveDeNoteEtudiantParSessionDTO result = metier.trouverLeReleverDeNoteParSessionDuEtudiant(id, semestre);
        return ResponseEntity.ok(result);
    }
}
