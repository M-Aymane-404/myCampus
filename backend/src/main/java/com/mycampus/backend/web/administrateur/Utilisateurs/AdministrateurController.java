package com.mycampus.backend.web.administrateur.Utilisateurs;

import com.mycampus.backend.dto.administrateur.DonneEntrer.AdministrateurEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.AdministrateurSortieDTO;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.metier.administrateur.utilisateurs.GestionUtilisateursMetier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/utilisateurs/administrateurs")
@RequiredArgsConstructor
@Slf4j
public class AdministrateurController {

    private final GestionUtilisateursMetier gestionUtilisateursMetier;

    @PostMapping("")
    public ResponseEntity<AdministrateurSortieDTO> creerAdministrateur(
            @RequestBody AdministrateurEntrerDTO administrateurEntrerDTO) {

        AdministrateurSortieDTO created =
                gestionUtilisateursMetier.creerAdministrateur(administrateurEntrerDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @GetMapping("/{id}")
    public ResponseEntity<AdministrateurSortieDTO> trouverAdministrateurParId(@PathVariable Long id) {

            AdministrateurSortieDTO administrateur = gestionUtilisateursMetier.trouverAdministrateurParId(id);
            return ResponseEntity.ok(administrateur);

    }

    @GetMapping("/ppr/{ppr}")
    public ResponseEntity<AdministrateurSortieDTO> trouverAdministrateurParPpr(@PathVariable String ppr) {

            AdministrateurSortieDTO administrateur = gestionUtilisateursMetier.trouverAdministrateurParPpr(ppr);
            return ResponseEntity.ok(administrateur);

    }

    @GetMapping("")
    public ResponseEntity<List<AdministrateurSortieDTO>> listerTousLesAdministrateurs() {

            List<AdministrateurSortieDTO> administrateurs = gestionUtilisateursMetier.listerTousLesAdministrateurs();
            return ResponseEntity.ok(administrateurs);

    }

    @PutMapping("/{id}")
    public ResponseEntity<AdministrateurSortieDTO> modifierAdministrateur(@PathVariable Long id, @RequestBody AdministrateurEntrerDTO administrateurEntrerDTO) throws UtilisateursException {

            AdministrateurSortieDTO updated = gestionUtilisateursMetier.modifierAdministrateur(id, administrateurEntrerDTO);
            return ResponseEntity.ok(updated);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerAdministrateur(@PathVariable Long id) throws UtilisateursException {

            gestionUtilisateursMetier.supprimerAdministrateur(id);
            return ResponseEntity.noContent().build();

    }

    @PatchMapping("/{id}/activer")
    public ResponseEntity<Void> activerAdministrateur(@PathVariable Long id) throws UtilisateursException {

            gestionUtilisateursMetier.activerAdministrateur(id);
            return ResponseEntity.noContent().build();

    }

    @PatchMapping("/{id}/desactiver")
    public ResponseEntity<Void> desactiverAdministrateur(@PathVariable Long id) throws UtilisateursException {

            gestionUtilisateursMetier.desactiverAdministrateur(id);
            return ResponseEntity.noContent().build();

    }





}
