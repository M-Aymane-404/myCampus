package com.mycampus.backend.web.administrateur.Utilisateurs;
import com.mycampus.backend.dto.FichierStocker.FichierStockerDTO;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.metier.administrateur.utilisateurs.GestionUtilisateursMetier;
 import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
@RestController
@RequestMapping("/api/admin/utilisateurs/")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN_UTILISATEUR_GERER')")
public class PhotoProfilController {

    private final GestionUtilisateursMetier gestionUtilisateursMetier;

    @PutMapping(value = "/{email}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FichierStockerDTO modifierPhotoProfil(
            @PathVariable String email,
            @RequestPart("photo") MultipartFile photo
    ) {
        Utilisateur u = gestionUtilisateursMetier.changerPhotoProfil(email, photo);
        return new FichierStockerDTO(u.getPhotoNom(),u.getPhotoUrl());

    }
}
