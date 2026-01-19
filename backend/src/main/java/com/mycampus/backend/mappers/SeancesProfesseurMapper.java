package com.mycampus.backend.mappers;



import com.mycampus.backend.dto.professeur.DonneeSortie.seances.SeanceResumeSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.seances.SeanceSortieDTO;
import com.mycampus.backend.entites.Seance;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SeancesProfesseurMapper {

    /**
     * This method converts one Seance entity to one SeanceSortieDTO for API response.
     * It copies simple fields and sets relation IDs to avoid exposing entities.
     */
    public SeanceSortieDTO FromSeanceVersSeanceSortieDTO(Seance seance) {
        if (seance == null) {
            return null;
        }

        SeanceSortieDTO seanceSortieDTO = new SeanceSortieDTO();

        BeanUtils.copyProperties(seance, seanceSortieDTO);

        seanceSortieDTO.setAffectationEnseignementId(
                seance.getAffectationEnseignement() != null ? seance.getAffectationEnseignement().getId() : null
        );

        seanceSortieDTO.setClasseId(
                seance.getAffectationEnseignement() != null && seance.getAffectationEnseignement().getClasse() != null
                        ? seance.getAffectationEnseignement().getClasse().getId()
                        : null
        );

        seanceSortieDTO.setMatiereId(
                seance.getAffectationEnseignement() != null && seance.getAffectationEnseignement().getMatiere() != null
                        ? seance.getAffectationEnseignement().getMatiere().getId()
                        : null
        );

        return seanceSortieDTO;
    }

    /**
     * This method converts one Seance entity to one SeanceResumeSortieDTO for list or calendar views.
     * It keeps only short fields to make responses smaller and faster.
     */
    public SeanceResumeSortieDTO FromSeanceVersSeanceResumeSortieDTO(Seance seance) {
        if (seance == null) {
            return null;
        }

        SeanceResumeSortieDTO seanceResumeSortieDTO = new SeanceResumeSortieDTO();
        BeanUtils.copyProperties(seance, seanceResumeSortieDTO);

        return seanceResumeSortieDTO;
    }

    /**
     * This method converts a list of Seance entities to a list of SeanceResumeSortieDTO.
     * It returns an empty list when the input list is null.
     */
    public List<SeanceResumeSortieDTO> FromListeSeanceVersListeSeanceResumeSortieDTO(List<Seance> seances) {
        if (seances == null) {
            return List.of();
        }

        return seances.stream()
                .map(this::FromSeanceVersSeanceResumeSortieDTO)
                .collect(Collectors.toList());
    }

    /**
     * This method converts a list of Seance entities to a list of SeanceSortieDTO.
     * It is useful when the frontend needs full details for many items.
     */
    public List<SeanceSortieDTO> FromListeSeanceVersListeSeanceSortieDTO(List<Seance> seances) {
        if (seances == null) {
            return List.of();
        }

        return seances.stream()
                .map(this::FromSeanceVersSeanceSortieDTO)
                .collect(Collectors.toList());
    }
}


