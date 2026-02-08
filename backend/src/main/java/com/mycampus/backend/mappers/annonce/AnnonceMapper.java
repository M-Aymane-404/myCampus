package com.mycampus.backend.mappers.annonce;

import com.mycampus.backend.dto.annonces.AnnonceSortieDTO;
import com.mycampus.backend.dto.annonces.AuteurDTO;
import com.mycampus.backend.dto.annonces.ClasseDTO;
import com.mycampus.backend.entites.Annonce;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnnonceMapper {

    public AnnonceSortieDTO changerVersSortieDTO(Annonce annonce) {
        AuteurDTO auteurDTO = AuteurDTO.builder()
                .id(annonce.getAuteur().getId())
                .nom(annonce.getAuteur().getNom())
                .prenom(annonce.getAuteur().getPrenom())
                .build();

         ClasseDTO classeDTO = null;
        if (annonce.getClasse() != null) {
            classeDTO = new  ClasseDTO(
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
                 annonce.isPubliee(),
                annonce.getCibleType(),
                auteurDTO,
                classeDTO
        );
    }


}
