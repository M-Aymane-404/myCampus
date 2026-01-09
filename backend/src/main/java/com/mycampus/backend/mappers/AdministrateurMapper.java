package com.mycampus.backend.mappers;

import com.mycampus.backend.entites.Administrateur;
import com.mycampus.backend.dto.AdministrateurDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AdministrateurMapper {

    public AdministrateurDTO versAdministrateurDTO(Administrateur administrateur) {
        if (administrateur == null) {
            return null;
        }
        
        return new AdministrateurDTO(
            administrateur.getId(),
            administrateur.getPpr(),
            administrateur.getNom(),
            administrateur.getPrenom(),
            administrateur.getEmail(),
            administrateur.getFonction(),
            administrateur.getGrade(),
            administrateur.getEnabled(),
            administrateur.getDateCreation(),
            administrateur.getDateDerniereConnexion()
        );
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
        administrateur.setId(administrateurDTO.getId());
        administrateur.setPpr(administrateurDTO.getPpr());
        administrateur.setNom(administrateurDTO.getNom());
        administrateur.setPrenom(administrateurDTO.getPrenom());
        administrateur.setEmail(administrateurDTO.getEmail());
        administrateur.setFonction(administrateurDTO.getFonction());
        administrateur.setGrade(administrateurDTO.getGrade());
        administrateur.setEnabled(administrateurDTO.isEnabled());
        administrateur.setDateCreation(administrateurDTO.getDateCreation());
        administrateur.setDateDerniereConnexion(administrateurDTO.getDateDerniereConnexion());
        
        return administrateur;
    }
}
