package com.mycampus.backend.mappers;

import com.mycampus.backend.dto.administrateur.DonneEntrer.AdministrateurEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.EtudiantEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.ProfesseurEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ProfesseurSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.EtudiantSortieDTO;
import com.mycampus.backend.entites.Administrateur;
import com.mycampus.backend.dto.administrateur.DonneSortie.AdministrateurSortieDTO;
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

    public AdministrateurSortieDTO FromAdministrateurVersAdministrateurSortieDTO(Administrateur administrateur) {
        if (administrateur == null) {
            return null;
        }
        
        AdministrateurSortieDTO administrateurSortieDTO = new AdministrateurSortieDTO();
        BeanUtils.copyProperties(administrateur, administrateurSortieDTO);
        return administrateurSortieDTO;
    }

    public List<AdministrateurSortieDTO> versListeAdministrateursDTO(List<Administrateur> administrateurs) {
        if (administrateurs == null) {
            return List.of();
        }
        
        return administrateurs.stream()
            .map(this::FromAdministrateurVersAdministrateurSortieDTO)
            .collect(Collectors.toList());
    }

    public Administrateur FromAdministrateurEntrerVersAdministrateur(AdministrateurEntrerDTO administrateurEntrerDTO) {
        if (administrateurEntrerDTO == null) {
            return null;
        }
        
        Administrateur administrateur = new Administrateur();
        BeanUtils.copyProperties(administrateurEntrerDTO, administrateur);
        return administrateur;
    }





    // ==================== PROFESSEUR ====================

    public ProfesseurSortieDTO FromProfesseurVersProfesseurSortirDTO(Professeur professeur) {
        if (professeur == null) {
            return null;
        }

        ProfesseurSortieDTO professeurSortieDTO = new ProfesseurSortieDTO();
        BeanUtils.copyProperties(professeur, professeurSortieDTO);
        
        // Mapping manuel des IDs de relations
        professeurSortieDTO.setCodeDepartementDirigee(professeur.getDepartementDirigee() != null ? professeur.getDepartementDirigee().getCodeDepartement() : null);
        professeurSortieDTO.setCodeDepartementOuTravail(professeur.getDepartementOuTravail() != null ? professeur.getDepartementOuTravail().getCodeDepartement() : null);
        professeurSortieDTO.setCodeFiliereDirigee(professeur.getFiliereDirigee() != null ? professeur.getFiliereDirigee().getCodeFiliere() : null);
        
        // Récupérer les matières enseignées (noms uniques) depuis les affectations actives
        if (professeur.getAffectationEnseignements() != null) {
            List<String> matieresEnseignees = professeur.getAffectationEnseignements().stream()
                .filter(ae -> ae.getActive() != null && ae.getActive()) // Seulement les affectations actives
                .map(ae -> ae.getMatiere().getNom()) // Récupérer le nom de la matière
                .distinct() // Éviter les doublons
                .collect(Collectors.toList());
            professeurSortieDTO.setMatieresEnseignees(matieresEnseignees);
            
            // Récupérer les filières (codes uniques) via classe.filiere
            List<String> filieres = professeur.getAffectationEnseignements().stream()
                .filter(ae -> ae.getActive() != null && ae.getActive()) // Seulement les affectations actives
                .map(ae -> ae.getClasse().getFiliere().getCodeFiliere()) // Récupérer le code de la filière
                .distinct() // Éviter les doublons
                .collect(Collectors.toList());
            professeurSortieDTO.setFilieres(filieres);
        }
        
        return professeurSortieDTO;
    }

    public List<ProfesseurSortieDTO> FromListeProfesseurVersListeProfesseurSortie(List<Professeur> professeurs) {
        if ( professeurs == null) {
            return List.of();
        }

        return professeurs.stream()
                .map(this::FromProfesseurVersProfesseurSortirDTO)
                .collect(Collectors.toList());
    }

    public Professeur FromProfesseurEntrerversProfesseur(ProfesseurEntrerDTO professeurEntrerDTO) {
        if (professeurEntrerDTO == null) {
            return null;
        }

        Professeur professeur = new Professeur();
        BeanUtils.copyProperties(professeurEntrerDTO, professeur);
        return professeur;
    }




    // ==================== ETUDIANT ====================

    public EtudiantSortieDTO FromEtudientVersEtudiantSortieDTO(Etudiant etudiant) {
        if (etudiant == null) {
            return null;
        }

        EtudiantSortieDTO etudiantSortieDTO = new EtudiantSortieDTO();
        BeanUtils.copyProperties(etudiant, etudiantSortieDTO);
        
        // Mapping manuel de l'ID de la classe
        etudiantSortieDTO.setCodeClasse(etudiant.getClasse() != null ? etudiant.getClasse().getCodeClasse() : null);
        
        return etudiantSortieDTO;
    }

    public List<EtudiantSortieDTO> FromListeEtudiantVersListeEtudiantsSortieDTO(List<Etudiant> etudiants) {
        if (etudiants == null) {
            return List.of();
        }

        return etudiants.stream()
                .map(this::FromEtudientVersEtudiantSortieDTO)
                .collect(Collectors.toList());
    }

    public Etudiant FromEtudiantEntrerversEtudiant(EtudiantEntrerDTO etudiantEntrerDTO) {
        if (etudiantEntrerDTO == null) {
            return null;
        }

        Etudiant etudiant = new Etudiant();
        BeanUtils.copyProperties(etudiantEntrerDTO, etudiant);
        return etudiant;
    }

}
