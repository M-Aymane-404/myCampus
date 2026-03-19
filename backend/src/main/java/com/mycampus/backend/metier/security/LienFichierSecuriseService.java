package com.mycampus.backend.metier.security;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LienFichierSecuriseService {

    public String construireUrlPhoto(String photoNom) {
        if (!StringUtils.hasText(photoNom)) {
            return null;
        }
        return "/api/fichiers/photos/" + photoNom;
    }

    public String construireUrlJustificatif(Long presenceId, String motifNom) {
        if (presenceId == null || !StringUtils.hasText(motifNom)) {
            return null;
        }
        return "/api/fichiers/justificatifs/presences/" + presenceId;
    }
}
