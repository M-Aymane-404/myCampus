package com.mycampus.backend.mappers;

import com.mycampus.backend.entites.Departement;
import com.mycampus.backend.entites.Filiere;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Module;
import com.mycampus.backend.entites.Matiere;
import com.mycampus.backend.entites.AffectationEnseignement;
import com.mycampus.backend.entites.Seance;
import com.mycampus.backend.dto.administrateur.DonneEntrer.DepartementEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.FiliereEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.ClasseEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.ModuleEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.MatiereEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.AffectationEnseignementEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.DepartementSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.FiliereSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ClasseSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ModuleSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.MatiereSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.AffectationEnseignementSortieDTO;
import com.mycampus.backend.dto.SeanceDTO;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StructureMapper {

    // ==================== DEPARTEMENT ====================

    public DepartementSortieDTO FromDepartementVersDepartementSortieDTO(Departement departement) {
        if (departement == null) {
            return null;
        }
        
        DepartementSortieDTO departementSortieDTO = new DepartementSortieDTO();
        BeanUtils.copyProperties(departement, departementSortieDTO);
        return departementSortieDTO;
    }

    public List<DepartementSortieDTO> FromListeDepartementVersListeDepartementSortieDTO(List<Departement> departements) {
        if (departements == null) {
            return List.of();
        }
        
        return departements.stream()
            .map(this::FromDepartementVersDepartementSortieDTO)
            .collect(Collectors.toList());
    }

    public Departement FromDepartementEntrerVersDepartement(DepartementEntrerDTO departementEntrerDTO) {
        if (departementEntrerDTO == null) {
            return null;
        }
        
        Departement departement = new Departement();
        BeanUtils.copyProperties(departementEntrerDTO, departement);
        return departement;
    }

    // ==================== FILIERE ====================

    public FiliereSortieDTO FromFiliereVersFiliereSortieDTO(Filiere filiere) {
        if (filiere == null) {
            return null;
        }

        FiliereSortieDTO filiereSortieDTO = new FiliereSortieDTO();
        BeanUtils.copyProperties(filiere, filiereSortieDTO);
        
        // Mapping manuel des IDs de relations
        if (filiere.getDepartement() != null) {
            filiereSortieDTO.setDepartementId(filiere.getDepartement().getId());
            filiereSortieDTO.setDepartementNom(filiere.getDepartement().getTypeDepartement());
        } else {
            filiereSortieDTO.setDepartementId(null);
            filiereSortieDTO.setDepartementNom(null);
        }
        
        filiereSortieDTO.setChef_filiere_id(filiere.getChefFiliere() != null ? filiere.getChefFiliere().getId() : null);

        return filiereSortieDTO;
    }

    public List<FiliereSortieDTO> FromListeFiliereVersListeFiliereSortieDTO(List<Filiere> filieres) {
        if (filieres == null) {
            return List.of();
        }
        
        return filieres.stream()
            .map(this::FromFiliereVersFiliereSortieDTO)
            .collect(Collectors.toList());
    }

    public Filiere FromFiliereEntrerVersFiliere(FiliereEntrerDTO filiereEntrerDTO) {
        if (filiereEntrerDTO == null) {
            return null;
        }
        
        Filiere filiere = new Filiere();
        BeanUtils.copyProperties(filiereEntrerDTO, filiere);
        return filiere;
    }

    // ==================== CLASSE ====================

    public ClasseSortieDTO FromClasseVersClasseSortieDTO(Classe classe) {
        if (classe == null) {
            return null;
        }

        ClasseSortieDTO classeSortieDTO = new ClasseSortieDTO();
        BeanUtils.copyProperties(classe, classeSortieDTO);
        
        // Mapping manuel de l'ID de la filière
        if (classe.getFiliere() != null) {
            classeSortieDTO.setFiliereId(classe.getFiliere().getId());
            classeSortieDTO.setFiliereNom(classe.getFiliere().getNom());
        } else {
            classeSortieDTO.setFiliereId(null);
            classeSortieDTO.setFiliereNom(null);
        }

        return classeSortieDTO;
    }

    public List<ClasseSortieDTO> FromListeClasseVersListeClasseSortieDTO(List<Classe> classes) {
        if (classes == null) {
            return List.of();
        }
        
        return classes.stream()
            .map(this::FromClasseVersClasseSortieDTO)
            .collect(Collectors.toList());
    }

    public Classe FromClasseEntrerVersClasse(ClasseEntrerDTO classeEntrerDTO) {
        if (classeEntrerDTO == null) {
            return null;
        }
        
        Classe classe = new Classe();
        BeanUtils.copyProperties(classeEntrerDTO, classe);
        return classe;
    }

    // ==================== MODULE ====================

    public ModuleSortieDTO FromModuleVersModuleSortieDTO(Module module) {
        if (module == null) {
            return null;
        }

        ModuleSortieDTO moduleSortieDTO = new ModuleSortieDTO();
        BeanUtils.copyProperties(module, moduleSortieDTO);
        
        // Mapping manuel de l'ID de la filière
        if (module.getFiliere() != null) {
            moduleSortieDTO.setFiliereId(module.getFiliere().getId());
            moduleSortieDTO.setFiliereNom(module.getFiliere().getNom());
        } else {
            moduleSortieDTO.setFiliereId(null);
            moduleSortieDTO.setFiliereNom(null);
        }

        return moduleSortieDTO;
    }

    public List<ModuleSortieDTO> FromListeModuleVersListeModuleSortieDTO(List<Module> modules) {
        if (modules == null) {
            return List.of();
        }
        
        return modules.stream()
            .map(this::FromModuleVersModuleSortieDTO)
            .collect(Collectors.toList());
    }

    public Module FromModuleEntrerVersModule(ModuleEntrerDTO moduleEntrerDTO) {
        if (moduleEntrerDTO == null) {
            return null;
        }
        
        Module module = new Module();
        BeanUtils.copyProperties(moduleEntrerDTO, module);
        return module;
    }

    // ==================== MATIERE ====================

    public MatiereSortieDTO FromMatiereVersMatiereSortieDTO(Matiere matiere) {
        if (matiere == null) {
            return null;
        }

        MatiereSortieDTO matiereSortieDTO = new MatiereSortieDTO();
        BeanUtils.copyProperties(matiere, matiereSortieDTO);
        
        // Mapping manuel de l'ID du module
        if (matiere.getModule() != null) {
            matiereSortieDTO.setModuleId(matiere.getModule().getId());
            matiereSortieDTO.setModuleNom(matiere.getModule().getNom());
        } else {
            matiereSortieDTO.setModuleId(null);
            matiereSortieDTO.setModuleNom(null);
        }

        return matiereSortieDTO;
    }

    public List<MatiereSortieDTO> FromListeMatiereVersListeMatiereSortieDTO(List<Matiere> matieres) {
        if (matieres == null) {
            return List.of();
        }
        
        return matieres.stream()
            .map(this::FromMatiereVersMatiereSortieDTO)
            .collect(Collectors.toList());
    }

    public Matiere FromMatiereEntrerVersMatiere(MatiereEntrerDTO matiereEntrerDTO) {
        if (matiereEntrerDTO == null) {
            return null;
        }
        
        Matiere matiere = new Matiere();
        BeanUtils.copyProperties(matiereEntrerDTO, matiere);
        return matiere;
    }

    // ==================== AFFECTATION ENSEIGNEMENT ====================

    public AffectationEnseignementSortieDTO FromAffectationEnseignementVersAffectationEnseignementSortieDTO(AffectationEnseignement affectation) {
        if (affectation == null) {
            return null;
        }
        
        AffectationEnseignementSortieDTO affectationSortieDTO = new AffectationEnseignementSortieDTO();
        BeanUtils.copyProperties(affectation, affectationSortieDTO);
        
        // Mapping manuel des relations
        if (affectation.getProfesseur() != null) {
            affectationSortieDTO.setProfesseurPpr(affectation.getProfesseur().getPpr());
            affectationSortieDTO.setProfesseurNom(affectation.getProfesseur().getPrenom() + " " + affectation.getProfesseur().getNom());
        } else {
            affectationSortieDTO.setProfesseurPpr(null);
            affectationSortieDTO.setProfesseurNom(null);
        }
        
        if (affectation.getClasse() != null) {
            affectationSortieDTO.setClasseId(affectation.getClasse().getId());
            affectationSortieDTO.setClasseNom(affectation.getClasse().getNom());
        } else {
            affectationSortieDTO.setClasseId(null);
            affectationSortieDTO.setClasseNom(null);
        }
        
        if (affectation.getMatiere() != null) {
            affectationSortieDTO.setMatiereId(affectation.getMatiere().getId());
            affectationSortieDTO.setMatiereNom(affectation.getMatiere().getNom());
        } else {
            affectationSortieDTO.setMatiereId(null);
            affectationSortieDTO.setMatiereNom(null);
        }

        return affectationSortieDTO;
    }

    public List<AffectationEnseignementSortieDTO> FromListeAffectationEnseignementVersListeAffectationEnseignementSortieDTO(List<AffectationEnseignement> affectations) {
        if (affectations == null) {
            return List.of();
        }
        
        return affectations.stream()
            .map(this::FromAffectationEnseignementVersAffectationEnseignementSortieDTO)
            .collect(Collectors.toList());
    }

    public AffectationEnseignement FromAffectationEnseignementEntrerVersAffectationEnseignement(AffectationEnseignementEntrerDTO affectationEnseignementEntrerDTO) {
        if (affectationEnseignementEntrerDTO == null) {
            return null;
        }
        
        AffectationEnseignement affectation = new AffectationEnseignement();
        BeanUtils.copyProperties(affectationEnseignementEntrerDTO, affectation);
        return affectation;
    }

    // ==================== SEANCE ====================

    public SeanceDTO FromSeanceVersSeanceDTO(Seance seance) {
        if (seance == null) {
            return null;
        }

        SeanceDTO seanceDTO = new SeanceDTO();
        BeanUtils.copyProperties(seance, seanceDTO);
        
        // Mapping manuel de l'ID de l'affectation
        seanceDTO.setAffectationEnseignementId(
            seance.getAffectationEnseignement() != null ? seance.getAffectationEnseignement().getId() : null
        );

        return seanceDTO;
    }

    public List<SeanceDTO> FromListeSeanceVersListeSeanceDTO(List<Seance> seances) {
        if (seances == null) {
            return List.of();
        }
        
        return seances.stream()
            .map(this::FromSeanceVersSeanceDTO)
            .collect(Collectors.toList());
    }
}
