package com.mycampus.backend.mappers.administrateur;

import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.*;
import com.mycampus.backend.dto.administrateur.DonneSortie.*;
import com.mycampus.backend.dto.etudiant.DonneeSortie.SeanceSortieEtudiantDTO;
import com.mycampus.backend.entites.*;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.DepartementEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.FiliereEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.ClasseEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.ModuleEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.MatiereEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.AffectationEnseignementEntrerDTO;
import com.mycampus.backend.entites.Module;
import com.mycampus.backend.repositorys.*;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor

public class StructureMapper {

    private final DepartementRepository departementRepository;
    private final FiliereRepository filiereRepository;
    private final EtudiantRepository etudiantRepository;
    private final MatiereRepository matiereRepository;
    private final ModuleRepository moduleRepository;
    private final AffectationEnseignementRepository affectationEnseignementRepository;
    private final  ProfesseurRepository professeurRepository;

    // ==================== DEPARTEMENT ====================

    public DepartementSortieDTO FromDepartementVersDepartementSortieDTO(Departement departement) {
        if (departement == null) {
            return null;
        }
        
        DepartementSortieDTO departementSortieDTO = new DepartementSortieDTO();
        BeanUtils.copyProperties(departement, departementSortieDTO);
        
        // Mapping des codes métier
      /*  departementSortieDTO.setCodeDepartement(departement.getCodeDepartement());
        departementSortieDTO.setTypeDepartement(departement.getTypeDepartement());
        departementSortieDTO.setDescription(departement.getDescription());
        departementSortieDTO.setDateCreation(departement.getDateCreation());*/
        departementSortieDTO.setNbFilieres( filiereRepository.countByDepartement_CodeDepartement(departement.getCodeDepartement()));
        departementSortieDTO.setNbEtudiants(etudiantRepository.countEtudiantsByCodeDepartement(departement.getCodeDepartement()));


        // Mapping du chef de département
        if (departement.getChefDepartement() != null) {
            departementSortieDTO.setChefDepartementPpr(departement.getChefDepartement().getPpr());
            departementSortieDTO.setChefDepartementNom(departement.getChefDepartement().getNom());
            departementSortieDTO.setChefDepartementPrenom(departement.getChefDepartement().getPrenom());
        }
        
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
        // Les relations seront gérées dans le service via requireX
        return departement;
    }

    public Departement FromDepartementModificationVersDepartement(DepartementModificationDTO departementModificationDTO) {
        if (departementModificationDTO == null) {
            return null;
        }

        Departement departement = new Departement();
        BeanUtils.copyProperties(departementModificationDTO, departement);
        // Les relations seront gérées dans le service via requireX
        return departement;
    }

    // ==================== FILIERE ====================

    public FiliereSortieDTO FromFiliereVersFiliereSortieDTO(Filiere filiere) {
        if (filiere == null) {
            return null;
        }

        FiliereSortieDTO filiereSortieDTO = new FiliereSortieDTO();
        BeanUtils.copyProperties(filiere, filiereSortieDTO);
        
        // Mapping des codes métier
       /* filiereSortieDTO.setCodeFiliere(filiere.getCodeFiliere());
        filiereSortieDTO.setNom(filiere.getNom());
        filiereSortieDTO.setDescription(filiere.getDescription());
        filiereSortieDTO.setDureeFormation(filiere.getDureeFormation());
        filiereSortieDTO.setNiveauDiplome(filiere.getNiveauDiplome());
        filiereSortieDTO.setEtat(filiere.getEtat());*/
        filiereSortieDTO.setNbModules(moduleRepository.countByFiliere_CodeFiliere(filiere.getCodeFiliere()));
        filiereSortieDTO.setNbEtudiants(etudiantRepository.countEtudiantsByCodeFiliere(filiere.getCodeFiliere()));
        
        // Mapping des relations par codes
        if (filiere.getDepartement() != null) {
            filiereSortieDTO.setCodeDepartement(filiere.getDepartement().getCodeDepartement());
            filiereSortieDTO.setDepartementNom(filiere.getDepartement().getTypeDepartement());
        }
        
        if (filiere.getChefFiliere() != null) {
            filiereSortieDTO.setChefFilierePpr(filiere.getChefFiliere().getPpr());
            filiereSortieDTO.setChefFiliereNom(filiere.getChefFiliere().getNom());
            filiereSortieDTO.setChefFilierePrenom(filiere.getChefFiliere().getPrenom());
        }
        
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
        // Les relations seront gérées dans le service via requireX
        return filiere;
    }

    public Filiere FromFiliereModificationVersFiliere(FiliereModificationDTO filiereModificationDTO) {
        if (filiereModificationDTO == null) {
            return null;
        }

        Filiere filiere = new Filiere();
        BeanUtils.copyProperties(filiereModificationDTO, filiere);
        // Les relations seront gérées dans le service via requireX
        return filiere;
    }

    // ==================== CLASSE ====================

    public ClasseSortieDTO FromClasseVersClasseSortieDTO(Classe classe) {
        if (classe == null) {
            return null;
        }

        ClasseSortieDTO classeSortieDTO = new ClasseSortieDTO();
        BeanUtils.copyProperties(classe, classeSortieDTO);
        
        // Mapping des codes métier
       /* classeSortieDTO.setCodeClasse(classe.getCodeClasse());
        classeSortieDTO.setNom(classe.getNom());
        classeSortieDTO.setNiveau(classe.getNiveau());
        classeSortieDTO.setAnneeUniversitaire(classe.getAnneeUniversitaire());
        classeSortieDTO.setEmploiTemps(classe.getEmploiTemps());*/

        classeSortieDTO.setEffectifMax(classe.getEffectifMax());
        
        // Mapping des relations par codes
        if (classe.getFiliere() != null) {
            classeSortieDTO.setCodeFiliere(classe.getFiliere().getCodeFiliere());
            classeSortieDTO.setFiliereNom(classe.getFiliere().getNom());
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
        // Les relations seront gérées dans le service via requireX
        return classe;
    }

    public Classe FromClasseModificationVersClasse(ClasseModificationDTO classeModificationDTO) {
        if (classeModificationDTO == null) {
            return null;
        }

        Classe classe = new Classe();
        BeanUtils.copyProperties(classeModificationDTO, classe);
        // Les relations seront gérées dans le service via requireX
        return classe;
    }

    // ==================== MODULE ====================

    public ModuleSortieDTO FromModuleVersModuleSortieDTO(Module module) {
        if (module == null) {
            return null;
        }

        ModuleSortieDTO moduleSortieDTO = new ModuleSortieDTO();
        BeanUtils.copyProperties(module, moduleSortieDTO);
        
        // Mapping des codes métier
     /*   moduleSortieDTO.setCodeModule(module.getCodeModule());
        moduleSortieDTO.setNom(module.getNom());
        moduleSortieDTO.setDescription(module.getDescription());
        moduleSortieDTO.setSemestre(module.getSemestre());
        moduleSortieDTO.setCoefficient(module.getCoefficient());*/
        moduleSortieDTO.setNbMatieres(matiereRepository.countByModule_CodeModule(moduleSortieDTO.getCodeModule()));

        // Mapping des relations par codes
        if (module.getFiliere() != null) {
            moduleSortieDTO.setCodeFiliere(module.getFiliere().getCodeFiliere());
            moduleSortieDTO.setFiliereNom(module.getFiliere().getNom());
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
        // Les relations seront gérées dans le service via requireX
        return module;
    }

    public Module FromModuleModificationVersModule(ModuleModificationDTO moduleModificationDTO) {
        if (moduleModificationDTO == null) {
            return null;
        }

        Module module = new Module();
        BeanUtils.copyProperties(moduleModificationDTO, module);
        // Les relations seront gérées dans le service via requireX
        return module;
    }

    // ==================== MATIERE ====================

    public MatiereSortieDTO FromMatiereVersMatiereSortieDTO(Matiere matiere) {
        if (matiere == null) {
            return null;
        }

        MatiereSortieDTO matiereSortieDTO = new MatiereSortieDTO();
        BeanUtils.copyProperties(matiere, matiereSortieDTO);
        
        // Mapping des codes métier
      /*  matiereSortieDTO.setCodeMatiere(matiere.getCodeMatiere());
        matiereSortieDTO.setNom(matiere.getNom());
        matiereSortieDTO.setDescription(matiere.getDescription());
        matiereSortieDTO.setCoefficient(matiere.getCoefficient());*/
        
        // Mapping des relations par codes
        if (matiere.getModule() != null) {
            matiereSortieDTO.setCodeModule(matiere.getModule().getCodeModule());
            matiereSortieDTO.setModuleNom(matiere.getModule().getNom());
        }
        
        return matiereSortieDTO;
    }

    public MatiereSortieAvecProfDTO FromMatiereVersMatiereSortieAvecProfDTO(Matiere matiere,String codeClasse) {
        if (matiere == null) {
            return null;
        }

        MatiereSortieAvecProfDTO matiereSortieAvecProfDTO = new MatiereSortieAvecProfDTO();
        BeanUtils.copyProperties(matiere, matiereSortieAvecProfDTO);

        // Mapping des codes métier
      /*  matiereSortieAvecProfDTO.setCodeMatiere(matiere.getCodeMatiere());
        matiereSortieAvecProfDTO.setNom(matiere.getNom());
        matiereSortieAvecProfDTO.setDescription(matiere.getDescription());
        matiereSortieAvecProfDTO.setCoefficient(matiere.getCoefficient());*/

        // Mapping des relations par codes

       AffectationEnseignement affectationEnseignement =  affectationEnseignementRepository.findByMatiere_CodeMatiereAndClasse_CodeClasse(matiere.getCodeMatiere(), codeClasse);
        if (affectationEnseignement != null) {
            ProfesseurMiniSortieDTO prof =  new ProfesseurMiniSortieDTO();
           prof.setPpr(affectationEnseignement.getProfesseur().getPpr());
           prof.setNom(affectationEnseignement.getProfesseur().getNom());
           prof.setPrenom(affectationEnseignement.getProfesseur().getPrenom());


            matiereSortieAvecProfDTO.setProfesseur(prof);
        }else {
            matiereSortieAvecProfDTO.setProfesseur(null);
        }

        return matiereSortieAvecProfDTO;
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
        // Les relations seront gérées dans le service via requireX
        return matiere;
    }

    public Matiere FromMatiereModificationVersMatiere(MatiereModificationDTO matiereModificationDTO) {
        if (matiereModificationDTO == null) {
            return null;
        }

        Matiere matiere = new Matiere();
        BeanUtils.copyProperties(matiereModificationDTO, matiere);
        // Les relations seront gérées dans le service via requireX
        return matiere;
    }

    // ==================== AFFECTATION ENSEIGNEMENT ====================

    public AffectationEnseignementSortieDTO FromAffectationEnseignementVersAffectationEnseignementSortieDTO(AffectationEnseignement affectation) {
        if (affectation == null) {
            return null;
        }
        
        AffectationEnseignementSortieDTO affectationSortieDTO = new AffectationEnseignementSortieDTO();
        BeanUtils.copyProperties(affectation, affectationSortieDTO);
        
        // Mapping des relations par codes
        if (affectation.getProfesseur() != null) {
            affectationSortieDTO.setProfesseurPpr(affectation.getProfesseur().getPpr());
            affectationSortieDTO.setProfesseurNom(affectation.getProfesseur().getPrenom() + " " + affectation.getProfesseur().getNom());
        }
        
        if (affectation.getClasse() != null) {
            affectationSortieDTO.setCodeClasse(affectation.getClasse().getCodeClasse());
            affectationSortieDTO.setClasseNom(affectation.getClasse().getNom());
        }
        
        if (affectation.getMatiere() != null) {
            affectationSortieDTO.setCodeMatiere(affectation.getMatiere().getCodeMatiere());
            affectationSortieDTO.setMatiereNom(affectation.getMatiere().getNom());
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
        // Les relations seront gérées dans le service via requireX
        return affectation;
    }

    public AffectationEnseignement FromAffectationEnseignementModificationVersAffectationEnseignement(AffectationEnseignementModificationDTO affectationEnseignementModificationDTO) {
        if (affectationEnseignementModificationDTO == null) {
            return null;
        }

        AffectationEnseignement affectation = new AffectationEnseignement();
        BeanUtils.copyProperties(affectationEnseignementModificationDTO, affectation);
        // Les relations seront gérées dans le service via requireX
        return affectation;
    }

    // ==================== SEANCE ====================

   /* public SeanceSortieEtudiantDTO FromSeanceVersSeanceSortieDTO(Seance seance) {
        if (seance == null) {
            return null;
        }

        SeanceSortieEtudiantDTO seanceDTO = new SeanceSortieEtudiantDTO();
        BeanUtils.copyProperties(seance, seanceDTO);
        
        // Mapping manuel de l'ID de l'affectation
        seanceDTO.setAffectationEnseignementId(
            seance.getAffectationEnseignement() != null ? seance.getAffectationEnseignement().getId() : null
        );

        return seanceDTO;
    }

    public List<SeanceSortieEtudiantDTO> FromListeSeanceVersListeSeanceSortieDTO(List<Seance> seances) {
        if (seances == null) {
            return List.of();
        }
        
        return seances.stream()
            .map(this::FromSeanceVersSeanceSortieDTO)
            .collect(Collectors.toList());
    }*/
}
