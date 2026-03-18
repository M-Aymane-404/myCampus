package com.mycampus.backend.mappers.annonce;

import com.mycampus.backend.dto.annonces.AnnonceSortieDTO;
import com.mycampus.backend.dto.annonces.AuteurDTO;
import com.mycampus.backend.dto.annonces.ClasseDTO;
import com.mycampus.backend.dto.annonces.DepartementDTO;
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
                .email(annonce.getAuteur().getEmail())
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
        DepartementDTO departementDTO = null;
        if (annonce.getDepartement() != null) {
            departementDTO =    DepartementDTO.builder()
                    .codeDepartement(annonce.getDepartement().getCodeDepartement())
                    .typeDepartement(annonce.getDepartement().getTypeDepartement())

                    .build();
        }


        return AnnonceSortieDTO.builder()
                .id(annonce.getId())
                .titre(annonce.getTitre())
                .contenu(annonce.getContenu())
                .datePublication(annonce.getDatePublication())
                .typeAnnonce(annonce.getTypeAnnonce())
                .publiee(annonce.isPubliee())
                .cibleType(annonce.getCibleType())
                .auteur(auteurDTO)
                .classe(classeDTO)
                .departement(departementDTO)
                .build();


    }


}
