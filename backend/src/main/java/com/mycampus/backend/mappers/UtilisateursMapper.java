package com.mycampus.backend.mappers;

import com.mycampus.backend.dto.ProfesseurDTO;
import com.mycampus.backend.dto.EtudiantDTO;
import com.mycampus.backend.entites.Administrateur;
import com.mycampus.backend.dto.AdministrateurDTO;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.entites.Etudiant;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UtilisateursMapper {

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





    // ==================== PROFESSEUR ====================

    public ProfesseurDTO versProfesseurDTO(Professeur professeur) {
        if (professeur == null) {
            return null;
        }

        ProfesseurDTO professeurDTO = new ProfesseurDTO();
        BeanUtils.copyProperties(professeur, professeurDTO);
        
        // Mapping manuel des IDs de relations
        professeurDTO.setDepartementDirigeeId(professeur.getDepartementDirigee() != null ? professeur.getDepartementDirigee().getId() : null);
        professeurDTO.setDepartementOuTravailId(professeur.getDepartementOuTravail() != null ? professeur.getDepartementOuTravail().getId() : null);
        professeurDTO.setFiliereDirigeeId(professeur.getFiliereDirigee() != null ? professeur.getFiliereDirigee().getId() : null);
        
        return professeurDTO;
    }

    public List<ProfesseurDTO> versListeProfesseur(List<Professeur> professeurs) {
        if ( professeurs == null) {
            return List.of();
        }

        return professeurs.stream()
                .map(this::versProfesseurDTO)
                .collect(Collectors.toList());
    }

    public Professeur versProfesseur(ProfesseurDTO professeurDTO) {
        if (professeurDTO == null) {
            return null;
        }

        Professeur professeur = new Professeur();
        BeanUtils.copyProperties(professeurDTO, professeur);
        return professeur;
    }




    // ==================== ETUDIANT ====================

    public EtudiantDTO versEtudiantDTO(Etudiant etudiant) {
        if (etudiant == null) {
            return null;
        }

        EtudiantDTO etudiantDTO = new EtudiantDTO();
        BeanUtils.copyProperties(etudiant, etudiantDTO);
        
        // Mapping manuel de l'ID de la classe
        etudiantDTO.setClasseId(etudiant.getClasse() != null ? etudiant.getClasse().getId() : null);
        
        return etudiantDTO;
    }

    public List<EtudiantDTO> versListeEtudiantsDTO(List<Etudiant> etudiants) {
        if (etudiants == null) {
            return List.of();
        }

        return etudiants.stream()
                .map(this::versEtudiantDTO)
                .collect(Collectors.toList());
    }

    public Etudiant versEtudiant(EtudiantDTO etudiantDTO) {
        if (etudiantDTO == null) {
            return null;
        }

        Etudiant etudiant = new Etudiant();
        BeanUtils.copyProperties(etudiantDTO, etudiant);
        return etudiant;
    }

}
