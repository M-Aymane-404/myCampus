package com.mycampus.backend.mappers.etudiant;

import com.mycampus.backend.dto.administrateur.DonneSortie.ProfesseurMiniSortieDTO;
import com.mycampus.backend.dto.etudiant.DonneeSortie.*;
import com.mycampus.backend.entites.*;
import com.mycampus.backend.entites.Module;
import com.mycampus.backend.repositorys.AffectationEnseignementRepository;
import com.mycampus.backend.repositorys.ProfesseurRepository;
import com.mycampus.backend.repositorys.SeanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor


public class StructureEtudiantMapper {


    private final AffectationEnseignementRepository affectationEnseignementRepository;
    private final SeanceRepository  seanceRepository;
    private final ProfesseurRepository professeurRepository;


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

    public MatiereSortieEtudiantDTO DuMatiereVersMatiereSortieEtudiantDTO(Matiere matiere, String codeClasse) {
        MatiereSortieEtudiantDTO matiereSortieEtudiantDTO = new MatiereSortieEtudiantDTO();
        BeanUtils.copyProperties(matiere, matiereSortieEtudiantDTO);

        AffectationEnseignement affectationEnseignement =  affectationEnseignementRepository.findByMatiere_CodeMatiereAndClasse_CodeClasse(matiere.getCodeMatiere(), codeClasse);
        if (affectationEnseignement != null) {
            matiereSortieEtudiantDTO.setProfesseurDeMatiere(affectationEnseignement.getProfesseur().getNom() + affectationEnseignement.getProfesseur().getPrenom());
        }else {
            matiereSortieEtudiantDTO.setProfesseurDeMatiere(null);
        }

        return matiereSortieEtudiantDTO;
    }


    public List<MatiereSortieEtudiantDTO> DuListeMatiereVersListeMatiereSortieEtudiantDTO(List<Matiere> matieres,String codeClasse) {
      if (matieres == null){
          return List.of();
      }

      return matieres.stream().map(matiere -> DuMatiereVersMatiereSortieEtudiantDTO(matiere,codeClasse)).toList();
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
        Seance seanceDuPresence = seanceRepository.findByPresence_presenceId(presence.getId());
        Professeur prof = professeurRepository.findBySeance_SeanceId(seanceDuPresence.getId());

        MiniSeanceSortieEtudiantDTO seance = new MiniSeanceSortieEtudiantDTO();

        seance.setDate(seanceDuPresence.getDate());
        seance.setSalle(seanceDuPresence.getSalle());
        seance.setType(seanceDuPresence.getType());
        seance.setProf(prof.getNom() + " " + prof.getPrenom());

        presenceSortieEtudiantDTO.setSeance(seance);


         return presenceSortieEtudiantDTO;
    }

    public List<PresenceSortieEtudiantDTO> DuListePresenceVersListePresenceSortieEtudiantDTO(List<Presence> presences) {
        if (presences == null){
            return List.of();
        }

        return presences.stream().map(this::DuPresenceVersPresenceSortieEtudiantDTO).toList();
    }



}
