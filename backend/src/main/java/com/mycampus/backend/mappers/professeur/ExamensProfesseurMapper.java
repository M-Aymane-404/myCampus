package com.mycampus.backend.mappers.professeur;

import com.mycampus.backend.dto.professeur.DonneeSortie.examens.ExamenRechercheSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.examens.ExamenSortieDTO;
import com.mycampus.backend.entites.Examen;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExamensProfesseurMapper {


    public static ExamenSortieDTO toSortieDTO(Examen e) {
        return ExamenSortieDTO.builder()
                .idExamen(e.getId())
                .codeMatiere(e.getMatiere().getCodeMatiere())
                .nomMatiere(e.getMatiere().getNom())
                .semestre(e.getSemestre())
                .numeroExamen(e.getNumeroExamen())
                .typeExamen(e.getTypeExamen())
                .dateExamen(e.getDateExamen())
                .duree(e.getDuree())
                .etat(e.getEtat())
                .corrigeDisponible(e.getCorrigePath() != null && !e.getCorrigePath().isBlank())
                .build();
    }

    public static ExamenRechercheSortieDTO toRechercheDTO(Examen e, String codeClasse, String nomClasse) {
        return ExamenRechercheSortieDTO.builder()
                .idExamen(e.getId())
                .codeClasse(codeClasse)
                .nomClasse(nomClasse)
                .codeMatiere(e.getMatiere().getCodeMatiere())
                .nomMatiere(e.getMatiere().getNom())
                .semestre(e.getSemestre())
                .numeroExamen(e.getNumeroExamen())
                .typeExamen(e.getTypeExamen())
                .etat(e.getEtat())
                .dateExamen(e.getDateExamen())
                .duree(e.getDuree())
                .corrigeDisponible(e.getCorrigePath() != null && !e.getCorrigePath().isBlank())
                .build();
    }
}
