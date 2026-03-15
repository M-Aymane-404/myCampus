package com.mycampus.backend.web.professeur;

import com.mycampus.backend.dto.professeur.DonneeEntrer.notes.SaisieNoteEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.notes.FeuilleNotesSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.notes.LigneNoteSortieDTO;
import com.mycampus.backend.metier.professeur.notes.GestionNotesProfesseurMetier;
import com.mycampus.backend.metier.security.CustomUserDetails;
import com.mycampus.backend.metier.security.GestionUtilisateurConnecteMetier;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/professeur/notes")
@RequiredArgsConstructor
public class NotesProfesseurController {

    private final GestionNotesProfesseurMetier notesMetier;
    private final GestionUtilisateurConnecteMetier utilisateurMetier;

    private String ppr(CustomUserDetails user) {
        return utilisateurMetier.trouverProfesseurPprParId(user.getId());
    }

    @GetMapping("/classes/{codeClasse}/examens/{examenId}/feuille")
    @PreAuthorize("hasAuthority('PROFESSEUR_NOTES_GERER')")
    public FeuilleNotesSortieDTO chargerFeuille(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String codeClasse,
            @PathVariable Long examenId) {

        return notesMetier.chargerFeuilleNotes(ppr(user), codeClasse, examenId);
    }
    @PutMapping("/classes/{codeClasse}/examens/{examenId}/resultats")
    @PreAuthorize("hasAuthority('PROFESSEUR_NOTES_GERER')")
    public LigneNoteSortieDTO autosave(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String codeClasse,
            @PathVariable Long examenId,
            @RequestBody SaisieNoteEntrerDTO dto) {

        return notesMetier.autosaveNote(ppr(user), codeClasse, examenId, dto);
    }

    @PostMapping("/classes/{codeClasse}/examens/{examenId}/publier")
    @PreAuthorize("hasAuthority('PROFESSEUR_NOTES_GERER')")
    public int publierNotes(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String codeClasse,
            @PathVariable Long examenId) {

        return notesMetier.publierNotes(ppr(principal), codeClasse, examenId);
    }

    @PostMapping(value = "/classes/{codeClasse}/examens/{examenId}/etudiants/{etudiantId}/copie",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PROFESSEUR_NOTES_GERER')")
    public LigneNoteSortieDTO deposerCopiecorigeedEtudiant(@AuthenticationPrincipal CustomUserDetails user,
                                           @PathVariable String codeClasse,
                                           @PathVariable Long examenId,
                                           @PathVariable Long etudiantId,
                                           @RequestPart("fichier") MultipartFile fichier) {
        return notesMetier.deposerCopieCorrigeeEtudiant(ppr(user), codeClasse, examenId, etudiantId, fichier);
    }

    @GetMapping("/classes/{codeClasse}/examens/{examenId}/etudiants/{etudiantId}/copie")
    @PreAuthorize("hasAuthority('PROFESSEUR_EXAMENS_GERER')")
    public ResponseEntity<Resource> telechargerCopieCorrigeExamendEtudiant(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String codeClasse,
            @PathVariable Long examenId,
            @PathVariable Long etudiantId
    ) {
        Resource resource = notesMetier.telechargerCopieCorrigeeEtudiantProf(ppr(user), codeClasse,examenId,etudiantId);

       /* String nomFichier = "copie_" + etudiant.getNom() + "_" + etudiant.getPrenom()
                + "_examen_" + examen.getNumeroExamen() + ".pdf";
*/
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"corrige_examen_" + etudiantId + "_examen_" + examenId + ".pdf\"")
                .body(resource);
    }
}
