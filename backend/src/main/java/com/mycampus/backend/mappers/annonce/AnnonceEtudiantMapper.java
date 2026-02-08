package com.mycampus.backend.mappers.annonce;

import com.mycampus.backend.dto.annonces.AnnonceSortieEtudiantDTO;
import com.mycampus.backend.entites.Annonce;
import org.springframework.stereotype.Component;

@Component
public class AnnonceEtudiantMapper {

    public AnnonceSortieEtudiantDTO changerVersSortieEtudiantDTO(Annonce annonce) {
        AnnonceSortieEtudiantDTO.AuteurDTO auteurDTO = new AnnonceSortieEtudiantDTO.AuteurDTO(
                annonce.getAuteur().getId(),
                annonce.getAuteur().getNom(),
                annonce.getAuteur().getPrenom()
        );

        String codeClasse = null;
        if (annonce.getClasse() != null) {
            codeClasse = annonce.getClasse().getCodeClasse();
        }

        return new AnnonceSortieEtudiantDTO(
                annonce.getId(),
                annonce.getTitre(),
                annonce.getContenu(),
                annonce.getDatePublication(),
                annonce.getDateExpiration(),
                annonce.isPubliee(),
                annonce.getCibleType(),
                codeClasse,
                auteurDTO
        );
    }
}
