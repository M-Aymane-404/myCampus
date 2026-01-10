package com.mycampus.backend.mappers;

import com.mycampus.backend.entites.Administrateur;
import com.mycampus.backend.dto.AdministrateurDTO;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdministrateurMapper {

    public AdministrateurDTO versAdministrateurDTO(Administrateur administrateur) {
        if (administrateur == null) {
            return null;
        }
        
        AdministrateurDTO administrateurDTO = new AdministrateurDTO();
        BeanUtils.copyProperties(administrateur, administrateurDTO);
        return administrateurDTO;
    }

    public List<AdministrateurDTO> versListeAdministrateursDTO(List<Administrateur> administrateurs) {
        if (administrateurs == null) {
            return List.of();
        }
        
        return administrateurs.stream()
            .map(this::versAdministrateurDTO)
            .collect(Collectors.toList());
    }

    public Administrateur versAdministrateur(AdministrateurDTO administrateurDTO) {
        if (administrateurDTO == null) {
            return null;
        }
        
        Administrateur administrateur = new Administrateur();
        BeanUtils.copyProperties(administrateurDTO, administrateur);
        return administrateur;
    }
}
