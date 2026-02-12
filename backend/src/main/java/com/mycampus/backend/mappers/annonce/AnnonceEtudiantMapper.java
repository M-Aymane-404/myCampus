package com.mycampus.backend.mappers.annonce;

import com.mycampus.backend.dto.annonces.AnnonceSortieEtudiantDTO;
import com.mycampus.backend.dto.annonces.AuteurEtudiantDTO;
import com.mycampus.backend.entites.Annonce;
import org.springframework.stereotype.Component;

@Component
public class AnnonceEtudiantMapper {

    public AnnonceSortieEtudiantDTO changerVersSortieEtudiantDTO(Annonce annonce) {
        AuteurEtudiantDTO auteurDTO =   AuteurEtudiantDTO.builder()
                .id(annonce.getAuteur().getId())
                .nom(annonce.getAuteur().getNom())
                .prenom(annonce.getAuteur().getPrenom())
                .build();

        String codeClasse = null;
        if (annonce.getClasse() != null) {
            codeClasse = annonce.getClasse().getCodeClasse();
        }




        return AnnonceSortieEtudiantDTO.builder()
                .id(annonce.getId())
                .titre(annonce.getTitre())
                .contenu(annonce.getContenu())
                .datePublication(annonce.getDatePublication())
                .publiee(annonce.isPubliee())
                .cibleType(annonce.getCibleType())
                .codeClasse(codeClasse)
                .auteur(auteurDTO)
                .build();
    }
}
