package com.mycampus.backend.mappers;

import com.mycampus.backend.entites.Departement;
import com.mycampus.backend.entites.Filiere;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Module;
import com.mycampus.backend.entites.Matiere;
import com.mycampus.backend.entites.AffectationEnseignement;
import com.mycampus.backend.entites.Seance;
import com.mycampus.backend.dto.DepartementDTO;
import com.mycampus.backend.dto.FiliereDTO;
import com.mycampus.backend.dto.ClasseDTO;
import com.mycampus.backend.dto.ModuleDTO;
import com.mycampus.backend.dto.MatiereDTO;
import com.mycampus.backend.dto.AffectationEnseignementDTO;
import com.mycampus.backend.dto.SeanceDTO;
import com.mycampus.backend.enumerations.Semestre;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StructureMapper {

    // Département mappings
    public DepartementDTO versDepartementDTO(Departement departement) {
        if (departement == null) {
            return null;
        }
        
        DepartementDTO departementDTO = new DepartementDTO();
        BeanUtils.copyProperties(departement, departementDTO);
        return departementDTO;
    }

    public List<DepartementDTO> versListeDepartementsDTO(List<Departement> departements) {
        if (departements == null) {
            return List.of();
        }
        
        return departements.stream()
            .map(this::versDepartementDTO)
            .collect(Collectors.toList());
    }

    public Departement versDepartement(DepartementDTO departementDTO) {
        if (departementDTO == null) {
            return null;
        }
        
        Departement departement = new Departement();
        BeanUtils.copyProperties(departementDTO, departement);
        return departement;
    }

    // Filière mappings
    public FiliereDTO versFiliereDTO(Filiere filiere) {
        if (filiere == null) {
            return null;
        }

        FiliereDTO filiereDTO = new FiliereDTO();
        BeanUtils.copyProperties(filiere, filiereDTO);
        
        // Mapping sécurisé - éviter le lazy loading
        if (filiere.getDepartement() != null) {
            filiereDTO.setChef_filiere_id(filiere.getChefFiliere() != null ? filiere.getChefFiliere().getId() : null);
            filiereDTO.setDepartementId(filiere.getDepartement().getId());
            filiereDTO.setDepartementNom(filiere.getDepartement().getTypeDepartement());
        } else {
            filiereDTO.setChef_filiere_id(null);
            filiereDTO.setDepartementId(null);
            filiereDTO.setDepartementNom(null);
        }

        return filiereDTO;
    }

    public List<FiliereDTO> versListeFilieresDTO(List<Filiere> filieres) {
        if (filieres == null) {
            return List.of();
        }
        
        return filieres.stream()
            .map(this::versFiliereDTO)
            .collect(Collectors.toList());
    }

    public Filiere versFiliere(FiliereDTO filiereDTO) {
        if (filiereDTO == null) {
            return null;
        }
        
        Filiere filiere = new Filiere();
        BeanUtils.copyProperties(filiereDTO, filiere);
        return filiere;
    }

    // Classe mappings
    public ClasseDTO versClasseDTO(Classe classe) {
        if (classe == null) {
            return null;
        }

        ClasseDTO classeDTO = new ClasseDTO();
        BeanUtils.copyProperties(classe, classeDTO);
        
        // Mapping sécurisé - éviter le lazy loading
        if (classe.getFiliere() != null) {
            classeDTO.setFiliereId(classe.getFiliere().getId());
            classeDTO.setFiliereNom(classe.getFiliere().getNom());
        } else {
            classeDTO.setFiliereId(null);
            classeDTO.setFiliereNom(null);
        }

        return classeDTO;
    }

    public List<ClasseDTO> versListeClassesDTO(List<Classe> classes) {
        if (classes == null) {
            return List.of();
        }
        
        return classes.stream()
            .map(this::versClasseDTO)
            .collect(Collectors.toList());
    }

    public Classe versClasse(ClasseDTO classeDTO) {
        if (classeDTO == null) {
            return null;
        }
        
        Classe classe = new Classe();
        BeanUtils.copyProperties(classeDTO, classe);
        return classe;
    }

    // Module mappings
    public ModuleDTO versModuleDTO(Module module) {
        if (module == null) {
            return null;
        }

        ModuleDTO moduleDTO = new ModuleDTO();
        BeanUtils.copyProperties(module, moduleDTO);
        
        // Mapping sécurisé - éviter le lazy loading
        if (module.getFiliere() != null) {
            moduleDTO.setFiliereId(module.getFiliere().getId());
            moduleDTO.setFiliereNom(module.getFiliere().getNom());
        } else {
            moduleDTO.setFiliereId(null);
            moduleDTO.setFiliereNom(null);
        }

        return moduleDTO;
    }

    public List<ModuleDTO> versListeModulesDTO(List<Module> modules) {
        if (modules == null) {
            return List.of();
        }
        
        return modules.stream()
            .map(this::versModuleDTO)
            .collect(Collectors.toList());
    }

    public Module versModule(ModuleDTO moduleDTO) {
        if (moduleDTO == null) {
            return null;
        }
        
        Module module = new Module();
        BeanUtils.copyProperties(moduleDTO, module);
        return module;
    }

    // Matière mappings
    public MatiereDTO versMatiereDTO(Matiere matiere) {
        if (matiere == null) {
            return null;
        }

        MatiereDTO matiereDTO = new MatiereDTO();
        BeanUtils.copyProperties(matiere, matiereDTO);
        matiereDTO.setModuleId(matiere.getModule() != null ? matiere.getModule().getId() : null);
        matiereDTO.setModuleNom(matiere.getModule() != null ? matiere.getModule().getNom() : null);
        
        return matiereDTO;
    }

    public List<MatiereDTO> versListeMatieresDTO(List<Matiere> matieres) {
        if (matieres == null) {
            return List.of();
        }
        
        return matieres.stream()
            .map(this::versMatiereDTO)
            .collect(Collectors.toList());
    }

    public Matiere versMatiere(MatiereDTO matiereDTO) {
        if (matiereDTO == null) {
            return null;
        }
        
        Matiere matiere = new Matiere();
        BeanUtils.copyProperties(matiereDTO, matiere);
        return matiere;
    }

    // AffectationEnseignement mappings
    public AffectationEnseignementDTO versAffectationEnseignementDTO(AffectationEnseignement affectation) {
        if (affectation == null) {
            return null;
        }
        
        AffectationEnseignementDTO affectationDTO = new AffectationEnseignementDTO();
        BeanUtils.copyProperties(affectation, affectationDTO);
        
        // Manual mapping for nested relations
        affectationDTO.setProfesseurPpr(affectation.getProfesseur() != null ? affectation.getProfesseur().getPpr() : null);
        affectationDTO.setProfesseurNom(affectation.getProfesseur() != null ? 
            affectation.getProfesseur().getPrenom() + " " + affectation.getProfesseur().getNom() : null);
        affectationDTO.setClasseId(affectation.getClasse() != null ? affectation.getClasse().getId() : null);
        affectationDTO.setClasseNom(affectation.getClasse() != null ? affectation.getClasse().getNom() : null);
        affectationDTO.setMatiereId(affectation.getMatiere() != null ? affectation.getMatiere().getId() : null);
        affectationDTO.setMatiereNom(affectation.getMatiere() != null ? affectation.getMatiere().getNom() : null);
        
        return affectationDTO;
    }

    public AffectationEnseignement versAffectationEnseignement(AffectationEnseignementDTO affectation) {
        if (affectation == null) {
            return null;
        }
        AffectationEnseignement affectationDTO = new AffectationEnseignement();
        BeanUtils.copyProperties(affectation, affectationDTO);
        return affectationDTO;
    }

    public List<AffectationEnseignementDTO> versListeAffectationEnseignementDTO(List<AffectationEnseignement> affectations) {
        return affectations.stream()
            .map(this::versAffectationEnseignementDTO)
            .collect(Collectors.toList());
    }

    // Seance mappings
    public SeanceDTO versSeanceDTO(Seance seance) {
        if (seance == null) {
            return null;
        }

        SeanceDTO seanceDTO = new SeanceDTO();
        BeanUtils.copyProperties(seance, seanceDTO);
        
        // Manual mapping for affectationEnseignement relation
        seanceDTO.setAffectationEnseignementId(
            seance.getAffectationEnseignement() != null ? seance.getAffectationEnseignement().getId() : null
        );

        return seanceDTO;
    }

    public List<SeanceDTO> versListeSeancesDTO(List<Seance> seances) {
        if (seances == null) {
            return List.of();
        }
        
        return seances.stream()
            .map(this::versSeanceDTO)
            .collect(Collectors.toList());
    }
}
