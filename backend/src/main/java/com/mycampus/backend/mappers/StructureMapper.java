package com.mycampus.backend.mappers;

import com.mycampus.backend.entites.Departement;
import com.mycampus.backend.entites.Filiere;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Module;
import com.mycampus.backend.entites.Matiere;
import com.mycampus.backend.dto.DepartementDTO;
import com.mycampus.backend.dto.FiliereDTO;
import com.mycampus.backend.dto.ClasseDTO;
import com.mycampus.backend.dto.ModuleDTO;
import com.mycampus.backend.dto.MatiereDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StructureMapper {

    // Département mappings
    public DepartementDTO versDepartementDTO(Departement departement) {
        if (departement == null) {
            return null;
        }
        
        return   DepartementDTO.builder()
                .id(departement.getId())
                .codeDepartement(departement.getCodeDepartement())
                .typeDepartement(departement.getTypeDepartement())
                .description(departement.getDescription())
                .dateCreation(departement.getDateCreation())
                .chefDepartement(departement.getChefDepartement())
                .build();

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
        departement.setId(departementDTO.getId());
        departement.setCodeDepartement(departementDTO.getCodeDepartement());
        departement.setTypeDepartement(departementDTO.getTypeDepartement());
        departement.setDescription(departementDTO.getDescription());

        
        return departement;
    }

    // Filière mappings
    public FiliereDTO versFiliereDTO(Filiere filiere) {
        if (filiere == null) {
            return null;
        }

        FiliereDTO filiereDTO = new FiliereDTO();

        BeanUtils.copyProperties(filiere, filiereDTO);
        filiereDTO.setChef_filiere_id(filiere.getChefFiliere() != null ? filiere.getChefFiliere().getId() : null);
        filiereDTO.setDepartementId( filiere.getDepartement() != null ? filiere.getDepartement().getId() : null);
        filiereDTO.setDepartementNom(filiere.getDepartement() != null ? filiere.getDepartement().getTypeDepartement() : null);


        return  filiereDTO;


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
        filiere.setId(filiereDTO.getId());
        filiere.setCodeFiliere(filiereDTO.getCodeFiliere());
        filiere.setNom(filiereDTO.getNom());
        filiere.setDescription(filiereDTO.getDescription());
        filiere.setDureeFormation(filiereDTO.getDureeFormation());
        filiere.setNiveauDiplome(filiereDTO.getNiveauDiplome());
        filiere.setEtat(filiereDTO.getEtat());

        
        return filiere;
    }

    // Classe mappings
    public ClasseDTO versClasseDTO(Classe classe) {
        if (classe == null) {
            return null;
        }


        ClasseDTO classeDTO = new ClasseDTO();
        BeanUtils.copyProperties(classe, classeDTO);
        classeDTO.setFiliereId(classe.getFiliere() != null ? classe.getFiliere().getId() : null);
        classeDTO.setFiliereNom(classe.getFiliere() != null ? classe.getFiliere().getNom() : null);

        return  classeDTO;

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
        moduleDTO.setClasseId(module.getClasse() != null ? module.getClasse().getId() : null);
        moduleDTO.setClasseNom(module.getClasse() != null ? module.getClasse().getNom() : null);

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
}
