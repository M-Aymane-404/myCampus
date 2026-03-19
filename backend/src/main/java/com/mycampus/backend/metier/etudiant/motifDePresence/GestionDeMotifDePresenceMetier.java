package com.mycampus.backend.metier.etudiant.motifDePresence;

import com.mycampus.backend.dto.FichierStocker.FichierStockerDTO;
import com.mycampus.backend.dto.etudiant.DonneeEntrer.EtudiantJustificationPresenceDTO;
import com.mycampus.backend.exceptions.PresenceException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface GestionDeMotifDePresenceMetier {
    FichierStockerDTO enregisterMotifDePresence(MultipartFile file);


    void ajouterJustification_Absence(Long idEtudiant, MultipartFile motif, Long presenceId, EtudiantJustificationPresenceDTO dto) throws PresenceException;

    Resource chargerMotifDePresence(String motifNom);
}
