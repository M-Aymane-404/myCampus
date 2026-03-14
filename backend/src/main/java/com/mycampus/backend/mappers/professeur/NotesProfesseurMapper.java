package com.mycampus.backend.mappers.professeur;

import com.mycampus.backend.dto.professeur.DonneeSortie.notes.LigneNoteSortieDTO;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Resultat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotesProfesseurMapper {

    public LigneNoteSortieDTO toLigneDTO(Etudiant etudiant, Resultat resultat) {
        return LigneNoteSortieDTO.builder()
                .idResultat(resultat != null ? resultat.getId() : null)
                .idEtudiant(etudiant != null ? etudiant.getId() : null)
                .apogee(etudiant != null ? etudiant.getApogee() : null)
                .nom(etudiant != null ? etudiant.getNom() : null)
                .prenom(etudiant != null ? etudiant.getPrenom() : null)
                .note(resultat != null ? resultat.getNote() : null)
                .publiee(resultat != null && resultat.getDatePublication() != null)
                .copieDisponible(
                        resultat != null
                                && resultat.getCopieEtudiantPath() != null
                                && !resultat.getCopieEtudiantPath().isBlank()
                )
                .build();
    }

}
