package com.mycampus.backend.mappers.professeur;

import com.mycampus.backend.dto.professeur.DonneeSortie.presences.LignePresenceSortieDTO;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Presence;
import com.mycampus.backend.enumerations.StatutJustificatif;
import com.mycampus.backend.metier.security.LienFichierSecuriseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor

public class PresenceMapper {

    private final LienFichierSecuriseService lienFichierSecuriseService;


    public LignePresenceSortieDTO FromPresencetoPresencceSortieDTO(Presence presence ) {
    if (presence == null) {
        return null;

    }

        Etudiant e = presence.getEtudiant();
        boolean justificationSoumise =
                presence.getTypeMotif() != null
                        || (presence.getDescriptionDeJustification() != null && !presence.getDescriptionDeJustification().isBlank())
                        || (presence.getMotifUrl() != null && !presence.getMotifUrl().isBlank())
                        || (presence.getMotifNom() != null && !presence.getMotifNom().isBlank());
        StatutJustificatif statutJustificatif = presence.getStatutJustificatif() != null
                ? presence.getStatutJustificatif()
                : (justificationSoumise ? StatutJustificatif.EN_ATTENTE : StatutJustificatif.AUCUN);

        return LignePresenceSortieDTO.builder()
                .idPresence(presence.getId())
                .idEtudiant(e != null ? e.getId() : null)
                .apogee(e != null ? e.getApogee() : null)
                .nomEtudiant(e != null ? e.getNom() : null)
                .prenomEtudiant(e != null ? e.getPrenom() : null)
                .status(presence.getStatus())
                .statutJustificatif(statutJustificatif)
                .justifiee(statutJustificatif == StatutJustificatif.VALIDE)
                .justificationSoumise(justificationSoumise)
                .motif(presence.getDescriptionDeJustification())
                .typeMotif(presence.getTypeMotif())
                .motifUrl(lienFichierSecuriseService.construireUrlJustificatif(presence.getId(), presence.getMotifNom()))
                .motifNom(presence.getMotifNom())
                .dateDeclaration(presence.getDateDeclaration())
                .build();
}}
