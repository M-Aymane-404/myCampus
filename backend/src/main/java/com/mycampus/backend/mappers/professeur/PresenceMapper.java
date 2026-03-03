package com.mycampus.backend.mappers.professeur;

import com.mycampus.backend.dto.professeur.DonneeSortie.presences.LignePresenceSortieDTO;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Presence;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor

public class PresenceMapper {



    public LignePresenceSortieDTO FromPresencetoPresencceSortieDTO(Presence presence ) {
    if (presence == null) {
        return null;

    }

        Etudiant e = presence.getEtudiant();

        return LignePresenceSortieDTO.builder()
                .idPresence(presence.getId())
                .idEtudiant(e != null ? e.getId() : null)
                .apogee(e != null ? e.getApogee() : null)
                .nomEtudiant(e != null ? e.getNom() : null)
                .prenomEtudiant(e != null ? e.getPrenom() : null)
                .status(presence.getStatus())
                .justifiee(presence.isValidationMotif())
                .motif(presence.getDescriptionDeJustification())
                .dateDeclaration(presence.getDateDeclaration())
                .build();
}}
