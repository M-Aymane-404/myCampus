package com.mycampus.backend.mappers.annonce;

import com.mycampus.backend.dto.annonces.AnnonceSortieDTO;
import com.mycampus.backend.entites.Annonce;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnnonceMapper {

    public AnnonceSortieDTO changerVersSortieDTO(Annonce annonce) {
        AnnonceSortieDTO.AuteurDTO auteurDTO = new AnnonceSortieDTO.AuteurDTO(
                annonce.getAuteur().getId(),
                annonce.getAuteur().getNom(),
                annonce.getAuteur().getPrenom(),
                annonce.getAuteur().getEmail()
        );

        AnnonceSortieDTO.ClasseDTO classeDTO = null;
        if (annonce.getClasse() != null) {
            classeDTO = new AnnonceSortieDTO.ClasseDTO(
                    annonce.getClasse().getId(),
                    annonce.getClasse().getCodeClasse(),
                    annonce.getClasse().getNom(),
                    annonce.getClasse().getNiveau()
            );
        }

        return new AnnonceSortieDTO(
                annonce.getId(),
                annonce.getTitre(),
                annonce.getContenu(),
                annonce.getDatePublication(),
                annonce.getDateExpiration(),
                annonce.isPubliee(),
                annonce.getCibleType(),
                auteurDTO,
                classeDTO
        );
    }


}
