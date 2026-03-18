package com.mycampus.backend.mappers.etudiant;

 import com.mycampus.backend.dto.administrateur.DonneSortie.EtudiantSortieDTO;
 import com.mycampus.backend.dto.etudiant.DonneeSortie.*;
import com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote.ExamenDeReleveEtudiantDTO;
import com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote.MatiereDeReleveEtudiantDTO;
import com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote.ModuleDeReleveEtudiantDTO;
 import com.mycampus.backend.entites.*;
import com.mycampus.backend.entites.Module;
import com.mycampus.backend.enumerations.StatutJustificatif;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor


public class StructureEtudiantMapper {





    public ModuleSortieEtudiantDTO DuModuleVersModuleSortieEtudiantDTO(Module module) {
        ModuleSortieEtudiantDTO moduleSortieEtudiantDTO = new ModuleSortieEtudiantDTO();
        BeanUtils.copyProperties(module, moduleSortieEtudiantDTO);

        return moduleSortieEtudiantDTO;
    }

    public List<ModuleSortieEtudiantDTO> DuListeModuleVersListModuleSortieEtudiantDTO(List<Module> modules) {
       if (modules == null){
           return List.of();
       }

       return modules.stream().map(this::DuModuleVersModuleSortieEtudiantDTO).toList();
    }

    public MatiereSortieEtudiantDTO DuMatiereVersMatiereSortieEtudiantDTO(Matiere matiere) {
        MatiereSortieEtudiantDTO matiereSortieEtudiantDTO = new MatiereSortieEtudiantDTO();
        BeanUtils.copyProperties(matiere, matiereSortieEtudiantDTO);



        return matiereSortieEtudiantDTO;
    }





    public SeanceSortieEtudiantDTO DuSeanceVersSeanceSortieEtudiantDTO(Seance seance) {

        SeanceSortieEtudiantDTO seanceSortieEtudiantDTO = new SeanceSortieEtudiantDTO();
        BeanUtils.copyProperties(seance, seanceSortieEtudiantDTO);

        return seanceSortieEtudiantDTO;

    }


    public List<SeanceSortieEtudiantDTO> DuListeSeanceVersListeSeanceSortieEtudiantDTO(List<Seance> seances) {
        if (seances == null){
            return List.of();
        }

        return seances.stream().map(this::DuSeanceVersSeanceSortieEtudiantDTO).toList();

    }
    public FichierSeanceSortieEtudiantDTO DuFichierSeanceVersFichierSeanceSortieEtudiantDTO(FichierSeance fichierSeance) {
        FichierSeanceSortieEtudiantDTO fichierSeanceSortieEtudiantDTO = new FichierSeanceSortieEtudiantDTO();
        BeanUtils.copyProperties(fichierSeance, fichierSeanceSortieEtudiantDTO);

        return fichierSeanceSortieEtudiantDTO;
    }


    public List<FichierSeanceSortieEtudiantDTO> DuListeFichierSeanceVersListeFichierSeanceSortieEtudiantDTO(List<FichierSeance> fichierSeance) {

        return fichierSeance.stream().map(this::DuFichierSeanceVersFichierSeanceSortieEtudiantDTO).toList();
    }


    public PresenceSortieEtudiantDTO DuPresenceVersPresenceSortieEtudiantDTO(Presence presence) {
        PresenceSortieEtudiantDTO presenceSortieEtudiantDTO = new PresenceSortieEtudiantDTO();
        BeanUtils.copyProperties(presence, presenceSortieEtudiantDTO);
        boolean justificationSoumise =
                presence.getTypeMotif() != null
                        || (presence.getDescriptionDeJustification() != null && !presence.getDescriptionDeJustification().isBlank())
                        || (presence.getMotifUrl() != null && !presence.getMotifUrl().isBlank())
                        || (presence.getMotifNom() != null && !presence.getMotifNom().isBlank());
        StatutJustificatif statutJustificatif = presence.getStatutJustificatif() != null
                ? presence.getStatutJustificatif()
                : (justificationSoumise ? StatutJustificatif.EN_ATTENTE : StatutJustificatif.AUCUN);
        presenceSortieEtudiantDTO.setStatutJustificatif(statutJustificatif);
        presenceSortieEtudiantDTO.setJustificationSoumise(justificationSoumise);
        presenceSortieEtudiantDTO.setJustificationValidee(statutJustificatif == StatutJustificatif.VALIDE);


         return presenceSortieEtudiantDTO;
    }












   public ExamenDeReleveEtudiantDTO DuExamenVersExamenDeReleveEtudiantDTO(Examen examen) {
        ExamenDeReleveEtudiantDTO examenDeReleveEtudiantDTO = new ExamenDeReleveEtudiantDTO();
        examenDeReleveEtudiantDTO.setNumeroExamen(examen.getNumeroExamen());
        examenDeReleveEtudiantDTO.setTypeExamen(examen.getTypeExamen());
        examenDeReleveEtudiantDTO.setCorrigePath(examen.getCorrigePath());
        examenDeReleveEtudiantDTO.setId(examen.getId());

        return examenDeReleveEtudiantDTO;
   }



    public MatiereDeReleveEtudiantDTO DuMatiereVersMatiereDeReleveEtudiantDTOO(Matiere matiere) {
        MatiereDeReleveEtudiantDTO matiereDeReleveEtudiantDTO = new MatiereDeReleveEtudiantDTO();
        matiereDeReleveEtudiantDTO.setCodeMatiere(matiere.getCodeMatiere());
        matiereDeReleveEtudiantDTO.setNomDuMatiere(matiere.getNom());
        matiereDeReleveEtudiantDTO.setId(matiere.getId());

        matiereDeReleveEtudiantDTO.setExamens(
                matiere.getExamens().stream().map(this::DuExamenVersExamenDeReleveEtudiantDTO).collect(Collectors.toList())
        );

        return matiereDeReleveEtudiantDTO;
    }

    public ModuleDeReleveEtudiantDTO DuModuleVersModuleDeReleveEtudiantDTO(Module module) {

        ModuleDeReleveEtudiantDTO moduleDeReleveEtudiantDTO = new ModuleDeReleveEtudiantDTO();
        moduleDeReleveEtudiantDTO.setCodeModule(module.getCodeModule());
        moduleDeReleveEtudiantDTO.setNomDuModule(module.getNom());
         moduleDeReleveEtudiantDTO.setId(module.getId());


        moduleDeReleveEtudiantDTO.setMatieres(
                module.getMatieres().stream().map(this::DuMatiereVersMatiereDeReleveEtudiantDTOO).toList()
        );

        return moduleDeReleveEtudiantDTO;
    }

    public EtudiantSortieEtudiantDTO FromEtudientVersEtudiantSortieDTO(Etudiant etudiant) {
        if (etudiant == null) {
            return null;
        }

        EtudiantSortieEtudiantDTO etudiantSortieDTO = new EtudiantSortieEtudiantDTO();
        BeanUtils.copyProperties(etudiant, etudiantSortieDTO);

        // Mapping manuel de l'ID de la classe
        etudiantSortieDTO.setCodeClasse(etudiant.getClasse() != null ? etudiant.getClasse().getCodeClasse() : null);

        return etudiantSortieDTO;
    }


}
