package com.mycampus.backend.mappers;

import com.mycampus.backend.dto.professeur.DonneeSortie.fichiersSeance.FichierSeanceSortieDTO;
import com.mycampus.backend.entites.FichierSeance;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FichiersSeanceProfesseurMapper {

    /**
     * This method converts one FichierSeance entity to one FichierSeanceSortieDTO for API response.
     * It copies metadata fields and sets seanceId to avoid exposing the Seance entity.
     */
    public FichierSeanceSortieDTO FromFichierSeanceVersFichierSeanceSortieDTO(FichierSeance fichierSeance) {
        if (fichierSeance == null) {
            return null;
        }

        FichierSeanceSortieDTO fichierSeanceSortieDTO = new FichierSeanceSortieDTO();

        BeanUtils.copyProperties(fichierSeance, fichierSeanceSortieDTO);

        fichierSeanceSortieDTO.setSeanceId(
                fichierSeance.getSeance() != null ? fichierSeance.getSeance().getId() : null
        );

        return fichierSeanceSortieDTO;
    }

    /**
     * This method converts a list of FichierSeance entities to a list of FichierSeanceSortieDTO.
     * It returns an empty list when the input list is null.
     */
    public List<FichierSeanceSortieDTO> FromListeFichierSeanceVersListeFichierSeanceSortieDTO(List<FichierSeance> fichiersSeance) {
        if (fichiersSeance == null) {
            return List.of();
        }

        return fichiersSeance.stream()
                .map(this::FromFichierSeanceVersFichierSeanceSortieDTO)
                .collect(Collectors.toList());
    }
}
