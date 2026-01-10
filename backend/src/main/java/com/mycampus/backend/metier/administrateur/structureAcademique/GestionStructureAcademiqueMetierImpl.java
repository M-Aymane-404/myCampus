package com.mycampus.backend.metier.administrateur.structureAcademique;

import com.mycampus.backend.dto.DepartementDTO;
import com.mycampus.backend.dto.FiliereDTO;
import com.mycampus.backend.dto.ClasseDTO;
import com.mycampus.backend.dto.ModuleDTO;
import com.mycampus.backend.dto.MatiereDTO;
import com.mycampus.backend.entites.Departement;
import com.mycampus.backend.entites.Filiere;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Module;
import com.mycampus.backend.entites.Matiere;
import com.mycampus.backend.repositorys.DepartementRepository;
import com.mycampus.backend.repositorys.FiliereRepository;
import com.mycampus.backend.repositorys.ClasseRepository;
import com.mycampus.backend.repositorys.ModuleRepository;
import com.mycampus.backend.repositorys.MatiereRepository;
import com.mycampus.backend.mappers.StructureMapper;
import com.mycampus.backend.exceptions.StructureAcademiqueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
;

@Service
@Transactional
@RequiredArgsConstructor
public class GestionStructureAcademiqueMetierImpl implements GestionStructureAcademiqueMetier {

    private final DepartementRepository departementRepository;
    private final FiliereRepository filiereRepository;
    private final ClasseRepository classeRepository;
    private final ModuleRepository moduleRepository;
    private final MatiereRepository matiereRepository;
    private final StructureMapper structureMapper;

    // Département management
    @Override
    public DepartementDTO creerDepartement(DepartementDTO departementDTO) throws StructureAcademiqueException {
        if (departementDTO == null) {
            throw new StructureAcademiqueException("Le département ne peut pas être nul");
        }
        
        if (departementDTO.getCodeDepartement() == null || departementDTO.getCodeDepartement().trim().isEmpty()) {
            throw new StructureAcademiqueException("Le code du département est obligatoire");
        }
        
        if (departementRepository.existsByCodeDepartement(departementDTO.getCodeDepartement())) {
            throw new StructureAcademiqueException("Un département avec ce code existe déjà");
        }

        
        Departement departement = structureMapper.versDepartement(departementDTO);
        departement.setDateCreation( LocalDate.now());
        Departement departementSauvegarde = departementRepository.save(departement);
        return structureMapper.versDepartementDTO(departementSauvegarde);
    }

    @Override
    public DepartementDTO trouverDepartementParId(Long id) throws StructureAcademiqueException {
        if (id == null) {
          throw  new StructureAcademiqueException(
                    "id est obligatoire"
            );
        }
        
        Departement departement = departementRepository.findById(id).orElseThrow(() ->
                new StructureAcademiqueException(
                        "Département non trouvé avec l'identifiant: " + id
                )
        );
        return structureMapper.versDepartementDTO(departement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartementDTO> listerTousLesDepartements() throws StructureAcademiqueException {
        try {
            List<Departement> departements = departementRepository.findAll();
            return structureMapper.versListeDepartementsDTO(departements);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la liste des départements: " + e.getMessage(), e);
        }
    }

    // Filière management
    @Override
    public FiliereDTO creerFiliere(FiliereDTO filiereDTO) throws StructureAcademiqueException {
        if (filiereDTO == null) {
            throw new StructureAcademiqueException("La filière ne peut pas être nulle");
        }
        
        if (filiereDTO.getCodeFiliere() == null || filiereDTO.getCodeFiliere().trim().isEmpty()) {
            throw new StructureAcademiqueException("Le code de la filière est obligatoire");
        }
        
        if (filiereRepository.existsByCodeFiliere(filiereDTO.getCodeFiliere())) {
            throw new StructureAcademiqueException("Une filière avec ce code existe déjà");
        }
        
        if (filiereDTO.getDepartementId() == null) {
            throw new StructureAcademiqueException("La filière doit être rattachée à un département");
        }
        
        Filiere filiere = structureMapper.versFiliere(filiereDTO);
        
        // Récupérer le département
        Departement departement = departementRepository.findById(filiereDTO.getDepartementId()).orElseThrow(() ->
                new StructureAcademiqueException("Département non trouvé avec l'identifiant: " + filiereDTO.getDepartementId()));

        filiere.setDepartement(departement);
        
        Filiere filiereSauvegardee = filiereRepository.save(filiere);
        return structureMapper.versFiliereDTO(filiereSauvegardee);
    }

    @Override
    public FiliereDTO trouverFiliereParId(Long id) throws StructureAcademiqueException {
        if (id == null) {
            throw  new StructureAcademiqueException(
                    "id est obligatoire"
            );
        }
        
        Filiere filiere = filiereRepository.findById(id).orElseThrow(() ->
                new StructureAcademiqueException("filière non trouvé avec l'identifiant: " + id ));
        return structureMapper.versFiliereDTO(filiere);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FiliereDTO> listerToutesLesFilieres() throws StructureAcademiqueException {
        try {
            List<Filiere> filieres = filiereRepository.findAll();
            return structureMapper.versListeFilieresDTO(filieres);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la liste des filières: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FiliereDTO> trouverFilieresParDepartement(Long departementId) throws StructureAcademiqueException {
        if (departementId == null) {
            return List.of();
        }
        
        try {
            List<Filiere> filieres = filiereRepository.findByDepartementId(departementId);
            return structureMapper.versListeFilieresDTO(filieres);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la recherche des filières par département: " + e.getMessage(), e);
        }
    }

    // Classe management
    @Override
    public ClasseDTO creerClasse(ClasseDTO classeDTO) throws StructureAcademiqueException {
        if (classeDTO == null) {
            throw new StructureAcademiqueException("La classe ne peut pas être nulle");
        }
        
        if (classeDTO.getCodeClasse() == null || classeDTO.getCodeClasse().trim().isEmpty()) {
            throw new StructureAcademiqueException("Le code de la classe est obligatoire");
        }
        
        if (classeRepository.existsByCodeClasse(classeDTO.getCodeClasse())) {
            throw new StructureAcademiqueException("Une classe avec ce code existe déjà");
        }
        
        if (classeDTO.getFiliereId() == null) {
            throw new StructureAcademiqueException("La classe doit être rattachée à une filière");
        }
        
        if (classeDTO.getEffectifMax() == null || classeDTO.getEffectifMax() <= 0) {
            throw new StructureAcademiqueException("L'effectif maximum est obligatoire et doit être supérieur à 0");
        }
        
        Classe classe = structureMapper.versClasse(classeDTO);
        
        // Récupérer la filière
        Filiere filiere = filiereRepository.findById(classeDTO.getFiliereId()).orElseThrow(()->
             new StructureAcademiqueException("Filière non trouvée avec l'identifiant: " + classeDTO.getFiliereId()));

        classe.setFiliere(filiere);
        
        Classe classeSauvegardee = classeRepository.save(classe);
        return structureMapper.versClasseDTO(classeSauvegardee);
    }

    @Override
    public ClasseDTO trouverClasseParId(Long id) throws StructureAcademiqueException {
        if (id == null) {
            throw  new StructureAcademiqueException(
                    "id est obligatoire"
            );
        }
        
        Classe classe = classeRepository.findById(id).orElseThrow(() ->
                new StructureAcademiqueException("Classe non trouvé avec l'identifiant: " + id));;
        return structureMapper.versClasseDTO(classe);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClasseDTO> listerToutesLesClasses() throws StructureAcademiqueException {
        try {
            List<Classe> classes = classeRepository.findAll();
            return structureMapper.versListeClassesDTO(classes);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la liste des classes: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClasseDTO> trouverClassesParFiliere(Long filiereId) throws StructureAcademiqueException {
        if (filiereId == null) {
            return List.of();
        }
        
        try {
            List<Classe> classes = classeRepository.findByFiliereId(filiereId);
            return structureMapper.versListeClassesDTO(classes);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la recherche des classes par filière: " + e.getMessage(), e);
        }
    }

    // Module management
    @Override
    public ModuleDTO creerModule(ModuleDTO moduleDTO) throws StructureAcademiqueException {
        if (moduleDTO == null) {
            throw new StructureAcademiqueException("Le module ne peut pas être nul");
        }
        
        if (moduleDTO.getCodeModule() == null || moduleDTO.getCodeModule().trim().isEmpty()) {
            throw new StructureAcademiqueException("Le code du module est obligatoire");
        }
        
        if (moduleRepository.existsByCodeModule(moduleDTO.getCodeModule())) {
            throw new StructureAcademiqueException("Un module avec ce code existe déjà");
        }
        
        if (moduleDTO.getFiliereId() == null) {
            throw new StructureAcademiqueException("Le module doit être rattaché à une filière");
        }
        
        Module module = structureMapper.versModule(moduleDTO);
        
        // Récupérer la filière
        Filiere filiere = filiereRepository.findById(moduleDTO.getFiliereId()).orElseThrow(() ->
            new StructureAcademiqueException("Filière non trouvée avec l'identifiant: " + moduleDTO.getFiliereId()));

        module.setFiliere(filiere);
        
        Module moduleSauvegarde = moduleRepository.save(module);
        return structureMapper.versModuleDTO(moduleSauvegarde);
    }

    @Override
    public ModuleDTO trouverModuleParId(Long id) throws StructureAcademiqueException {
        if (id == null) {
            throw  new StructureAcademiqueException(
                    "id est obligatoire"
            );
        }
        
        Module module = moduleRepository.findById(id).orElseThrow(() ->
                new StructureAcademiqueException("Module non trouvé avec l'identifiant: " + id));;;
        return structureMapper.versModuleDTO(module);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleDTO> listerTousLesModules() throws StructureAcademiqueException {
        try {
            List<Module> modules = moduleRepository.findAll();
            return structureMapper.versListeModulesDTO(modules);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la liste des modules: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleDTO> trouverModulesParFiliere(Long filiereId) throws StructureAcademiqueException {
        if (filiereId == null) {
            return List.of();
        }
        
        try {
            List<Module> modules = moduleRepository.findByFiliereId(filiereId);
            return structureMapper.versListeModulesDTO(modules);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la recherche des modules par filière: " + e.getMessage(), e);
        }
    }

    // Matière management
    @Override
    public MatiereDTO creerMatiere(MatiereDTO matiereDTO) throws StructureAcademiqueException {
        if (matiereDTO == null) {
            throw new StructureAcademiqueException("La matière ne peut pas être nulle");
        }
        
        if (matiereDTO.getCodeMatiere() == null || matiereDTO.getCodeMatiere().trim().isEmpty()) {
            throw new StructureAcademiqueException("Le code de la matière est obligatoire");
        }
        
        if (matiereRepository.existsByCodeMatiere(matiereDTO.getCodeMatiere())) {
            throw new StructureAcademiqueException("Une matière avec ce code existe déjà");
        }
        
        if (matiereDTO.getModuleId() == null) {
            throw new StructureAcademiqueException("La matière doit être rattachée à un module");
        }
        
        Matiere matiere = structureMapper.versMatiere(matiereDTO);
        
        // Récupérer le module
        Module module = moduleRepository.findById(matiereDTO.getModuleId()).orElseThrow(()->
                new StructureAcademiqueException("Module non trouvé avec l'identifiant: " + matiereDTO.getModuleId()));

        matiere.setModule(module);
        
        Matiere matiereSauvegardee = matiereRepository.save(matiere);
        return structureMapper.versMatiereDTO(matiereSauvegardee);
    }

    @Override
    public MatiereDTO trouverMatiereParId(Long id) throws StructureAcademiqueException {
        if (id == null) {
            throw  new StructureAcademiqueException(
                    "id est obligatoire"
            );
        }
        
        Matiere matiere = matiereRepository.findById(id).orElseThrow(() ->
                new StructureAcademiqueException("Matière non trouvé avec l'identifiant: " + id));;
        return structureMapper.versMatiereDTO(matiere);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatiereDTO> listerToutesLesMatieres() throws StructureAcademiqueException {
        try {
            List<Matiere> matieres = matiereRepository.findAll();
            return structureMapper.versListeMatieresDTO(matieres);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la liste des matières: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatiereDTO> trouverMatieresParModule(Long moduleId) throws StructureAcademiqueException {
        if (moduleId == null) {
            return List.of();
        }
        
        try {
            List<Matiere> matieres = matiereRepository.findByModuleId(moduleId);
            return structureMapper.versListeMatieresDTO(matieres);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la recherche des matières par module: " + e.getMessage(), e);
        }
    }
}
