package com.mycampus.backend.metier.administrateur.PhotoStorage;

import com.mycampus.backend.dto.FichierStocker.FichierStockerDTO;
import org.springframework.web.multipart.MultipartFile;

public interface PhotoStorageMetier {
    FichierStockerDTO enregistrerPhoto(MultipartFile file);

    void supprimerPhotoUtilisateurSiExsist(String photoNom);
}
