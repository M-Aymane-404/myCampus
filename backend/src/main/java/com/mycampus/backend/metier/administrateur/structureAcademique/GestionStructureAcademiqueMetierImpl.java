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
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.AffectationEnseignement;
import com.mycampus.backend.entites.Examen;
import com.mycampus.backend.repositorys.DepartementRepository;
import com.mycampus.backend.repositorys.FiliereRepository;
import com.mycampus.backend.repositorys.ClasseRepository;
import com.mycampus.backend.repositorys.ModuleRepository;
import com.mycampus.backend.repositorys.MatiereRepository;
import com.mycampus.backend.repositorys.EtudiantRepository;
import com.mycampus.backend.repositorys.AffectationEnseignementRepository;
import com.mycampus.backend.repositorys.ExamenRepository;
import com.mycampus.backend.mappers.StructureMapper;
import com.mycampus.backend.exceptions.StructureAcademiqueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GestionStructureAcademiqueMetierImpl implements GestionStructureAcademiqueMetier {

    private final DepartementRepository departementRepository;
    private final FiliereRepository filiereRepository;
    private final ClasseRepository classeRepository;
    private final ModuleRepository moduleRepository;
    private final MatiereRepository matiereRepository;
    private final EtudiantRepository etudiantRepository;
    private final AffectationEnseignementRepository affectationEnseignementRepository;
    private final ExamenRepository examenRepository;
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

    @Override
    public DepartementDTO modifierDepartement(Long departementId, DepartementDTO departementDTO) throws StructureAcademiqueException {
        if (departementId == null) {
            throw new StructureAcademiqueException("L'identifiant du département est obligatoire");
        }
        
        if (departementDTO == null) {
            throw new StructureAcademiqueException("Le département ne peut pas être nul");
        }
        
        // Charger l'entité existante
        Departement departement = departementRepository.findById(departementId).orElseThrow(() ->
                new StructureAcademiqueException("Département non trouvé avec l'identifiant: " + departementId));
        
        // Valider l'unicité du code si modifié
        if (departementDTO.getCodeDepartement() != null && 
            !departementDTO.getCodeDepartement().equals(departement.getCodeDepartement()) &&
            departementRepository.existsByCodeDepartement(departementDTO.getCodeDepartement())) {
            throw new StructureAcademiqueException("Un département avec ce code existe déjà");
        }
        
        // Mettre à jour les champs autorisés
        if (departementDTO.getCodeDepartement() != null && !departementDTO.getCodeDepartement().trim().isEmpty()) {
            departement.setCodeDepartement(departementDTO.getCodeDepartement());
        }
        if (departementDTO.getTypeDepartement() != null && !departementDTO.getTypeDepartement().trim().isEmpty()) {
            departement.setTypeDepartement(departementDTO.getTypeDepartement());
        }
        if (departementDTO.getDescription() != null) {
            departement.setDescription(departementDTO.getDescription());
        }
        
        Departement departementModifie = departementRepository.save(departement);
        return structureMapper.versDepartementDTO(departementModifie);
    }
    
    @Override
    public void supprimerDepartement(Long departementId) throws StructureAcademiqueException {
        if (departementId == null) {
            throw new StructureAcademiqueException("L'identifiant du département est obligatoire");
        }
        
        Departement departement = departementRepository.findById(departementId).orElseThrow(() ->
                new StructureAcademiqueException("Département non trouvé avec l'identifiant: " + departementId));
        
        // Vérifier les dépendances
        List<Filiere> filieres = filiereRepository.findByDepartementId(departementId);
        if (!filieres.isEmpty()) {
            throw new StructureAcademiqueException("Suppression impossible : le département contient des filières");
        }
        
        departementRepository.delete(departement);
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
                new StructureAcademiqueException("Département introuvable avec l'identifiant: " + filiereDTO.getDepartementId()));

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
            throw new StructureAcademiqueException("L'identifiant du département est obligatoire");
        }
        
        try {
            List<Filiere> filieres = filiereRepository.findByDepartementId(departementId);
            return structureMapper.versListeFilieresDTO(filieres);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la recherche des filières par département: " + e.getMessage(), e);
        }
    }

    @Override
    public FiliereDTO modifierFiliere(Long filiereId, FiliereDTO filiereDTO) throws StructureAcademiqueException {
        if (filiereId == null) {
            throw new StructureAcademiqueException("L'identifiant de la filière est obligatoire");
        }
        
        if (filiereDTO == null) {
            throw new StructureAcademiqueException("La filière ne peut pas être nulle");
        }
        
        // Charger l'entité existante
        Filiere filiere = filiereRepository.findById(filiereId).orElseThrow(() ->
                new StructureAcademiqueException("Filière non trouvée avec l'identifiant: " + filiereId));
        
        // Valider l'unicité du code si modifié
        if (filiereDTO.getCodeFiliere() != null && 
            !filiereDTO.getCodeFiliere().equals(filiere.getCodeFiliere()) &&
            filiereRepository.existsByCodeFiliere(filiereDTO.getCodeFiliere())) {
            throw new StructureAcademiqueException("Une filière avec ce code existe déjà");
        }
        
        // Valider le département si modifié
        if (filiereDTO.getDepartementId() != null && !filiereDTO.getDepartementId().equals(filiere.getDepartement().getId())) {
            Departement departement = departementRepository.findById(filiereDTO.getDepartementId()).orElseThrow(() ->
                    new StructureAcademiqueException("Département introuvable avec l'identifiant: " + filiereDTO.getDepartementId()));
            filiere.setDepartement(departement);
        }
        
        // Mettre à jour les champs autorisés
        if (filiereDTO.getCodeFiliere() != null && !filiereDTO.getCodeFiliere().trim().isEmpty()) {
            filiere.setCodeFiliere(filiereDTO.getCodeFiliere());
        }
        if (filiereDTO.getNom() != null && !filiereDTO.getNom().trim().isEmpty()) {
            filiere.setNom(filiereDTO.getNom());
        }
        if (filiereDTO.getDescription() != null) {
            filiere.setDescription(filiereDTO.getDescription());
        }
        if (filiereDTO.getEmploiTemps() != null) {
            filiere.setEmploiTemps(filiereDTO.getEmploiTemps());
        }
        if (filiereDTO.getDureeFormation() != null && !filiereDTO.getDureeFormation().trim().isEmpty()) {
            filiere.setDureeFormation(filiereDTO.getDureeFormation());
        }
        if (filiereDTO.getNiveauDiplome() != null && !filiereDTO.getNiveauDiplome().trim().isEmpty()) {
            filiere.setNiveauDiplome(filiereDTO.getNiveauDiplome());
        }
        if (filiereDTO.getEtat() != null) {
            filiere.setEtat(filiereDTO.getEtat());
        }
        
        Filiere filiereModifiee = filiereRepository.save(filiere);
        return structureMapper.versFiliereDTO(filiereModifiee);
    }
    
    @Override
    public void supprimerFiliere(Long filiereId) throws StructureAcademiqueException {
        if (filiereId == null) {
            throw new StructureAcademiqueException("L'identifiant de la filière est obligatoire");
        }
        
        Filiere filiere = filiereRepository.findById(filiereId).orElseThrow(() ->
                new StructureAcademiqueException("Filière non trouvée avec l'identifiant: " + filiereId));
        
        // Vérifier les dépendances
        List<Classe> classes = classeRepository.findByFiliereId(filiereId);
        if (!classes.isEmpty()) {
            throw new StructureAcademiqueException("Suppression impossible : la filière contient des classes");
        }
        
        List<Module> modules = moduleRepository.findByFiliereId(filiereId);
        if (!modules.isEmpty()) {
            throw new StructureAcademiqueException("Suppression impossible : la filière contient des modules");
        }
        
        filiereRepository.delete(filiere);
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
             new StructureAcademiqueException("Filière introuvable avec l'identifiant: " + classeDTO.getFiliereId()));

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
            throw new StructureAcademiqueException("L'identifiant de la filière est obligatoire");
        }
        
        try {
            List<Classe> classes = classeRepository.findByFiliereId(filiereId);
            return structureMapper.versListeClassesDTO(classes);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la recherche des classes par filière: " + e.getMessage(), e);
        }
    }

    @Override
    public ClasseDTO modifierClasse(Long classeId, ClasseDTO classeDTO) throws StructureAcademiqueException {
        if (classeId == null) {
            throw new StructureAcademiqueException("L'identifiant de la classe est obligatoire");
        }
        
        if (classeDTO == null) {
            throw new StructureAcademiqueException("La classe ne peut pas être nulle");
        }
        
        // Charger l'entité existante
        Classe classe = classeRepository.findById(classeId).orElseThrow(() ->
                new StructureAcademiqueException("Classe non trouvée avec l'identifiant: " + classeId));
        
        // Valider l'unicité du code si modifié
        if (classeDTO.getCodeClasse() != null && 
            !classeDTO.getCodeClasse().equals(classe.getCodeClasse()) &&
            classeRepository.existsByCodeClasse(classeDTO.getCodeClasse())) {
            throw new StructureAcademiqueException("Une classe avec ce code existe déjà");
        }
        
        // Valider la filière si modifiée
        if (classeDTO.getFiliereId() != null && !classeDTO.getFiliereId().equals(classe.getFiliere().getId())) {
            Filiere filiere = filiereRepository.findById(classeDTO.getFiliereId()).orElseThrow(() ->
                    new StructureAcademiqueException("Filière introuvable avec l'identifiant: " + classeDTO.getFiliereId()));
            classe.setFiliere(filiere);
        }
        
        // Valider effectifMax si modifié
        if (classeDTO.getEffectifMax() != null && classeDTO.getEffectifMax() <= 0) {
            throw new StructureAcademiqueException("L'effectif maximum doit être supérieur à 0");
        }
        
        // Mettre à jour les champs autorisés
        if (classeDTO.getCodeClasse() != null && !classeDTO.getCodeClasse().trim().isEmpty()) {
            classe.setCodeClasse(classeDTO.getCodeClasse());
        }
        if (classeDTO.getNom() != null && !classeDTO.getNom().trim().isEmpty()) {
            classe.setNom(classeDTO.getNom());
        }
        if (classeDTO.getNiveau() != null && !classeDTO.getNiveau().trim().isEmpty()) {
            classe.setNiveau(classeDTO.getNiveau());
        }
        if (classeDTO.getAnneeUniversitaire() != null && !classeDTO.getAnneeUniversitaire().trim().isEmpty()) {
            classe.setAnneeUniversitaire(classeDTO.getAnneeUniversitaire());
        }
        if (classeDTO.getEffectifMax() != null) {
            classe.setEffectifMax(classeDTO.getEffectifMax());
        }
        
        Classe classeModifiee = classeRepository.save(classe);
        return structureMapper.versClasseDTO(classeModifiee);
    }
    
    @Override
    public void supprimerClasse(Long classeId) throws StructureAcademiqueException {
        if (classeId == null) {
            throw new StructureAcademiqueException("L'identifiant de la classe est obligatoire");
        }
        
        Classe classe = classeRepository.findById(classeId).orElseThrow(() ->
                new StructureAcademiqueException("Classe non trouvée avec l'identifiant: " + classeId));
        
        // Vérifier les dépendances
        List<Etudiant> etudiants = etudiantRepository.findByClasseId(classeId);
        if (!etudiants.isEmpty()) {
            throw new StructureAcademiqueException("Suppression impossible : la classe contient des étudiants");
        }
        
        classeRepository.delete(classe);
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
            new StructureAcademiqueException("Filière introuvable avec l'identifiant: " + moduleDTO.getFiliereId()));

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
            throw new StructureAcademiqueException("L'identifiant de la filière est obligatoire");
        }
        
        try {
            List<Module> modules = moduleRepository.findByFiliereId(filiereId);
            return structureMapper.versListeModulesDTO(modules);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la recherche des modules par filière: " + e.getMessage(), e);
        }
    }

    @Override
    public ModuleDTO modifierModule(Long moduleId, ModuleDTO moduleDTO) throws StructureAcademiqueException {
        if (moduleId == null) {
            throw new StructureAcademiqueException("L'identifiant du module est obligatoire");
        }
        
        if (moduleDTO == null) {
            throw new StructureAcademiqueException("Le module ne peut pas être nul");
        }
        
        // Charger l'entité existante
        Module module = moduleRepository.findById(moduleId).orElseThrow(() ->
                new StructureAcademiqueException("Module non trouvé avec l'identifiant: " + moduleId));
        
        // Valider l'unicité du code si modifié
        if (moduleDTO.getCodeModule() != null && 
            !moduleDTO.getCodeModule().equals(module.getCodeModule()) &&
            moduleRepository.existsByCodeModule(moduleDTO.getCodeModule())) {
            throw new StructureAcademiqueException("Un module avec ce code existe déjà");
        }
        
        // Valider la filière si modifiée
        if (moduleDTO.getFiliereId() != null && !moduleDTO.getFiliereId().equals(module.getFiliere().getId())) {
            Filiere filiere = filiereRepository.findById(moduleDTO.getFiliereId()).orElseThrow(() ->
                    new StructureAcademiqueException("Filière non trouvée avec l'identifiant: " + moduleDTO.getFiliereId()));
            module.setFiliere(filiere);
        }
        
        // Valider coefficient si modifié
        if (moduleDTO.getCoefficient() != null && moduleDTO.getCoefficient() <= 0) {
            throw new StructureAcademiqueException("Le coefficient doit être supérieur à 0");
        }
        
        // Mettre à jour les champs autorisés
        if (moduleDTO.getCodeModule() != null && !moduleDTO.getCodeModule().trim().isEmpty()) {
            module.setCodeModule(moduleDTO.getCodeModule());
        }
        if (moduleDTO.getNom() != null && !moduleDTO.getNom().trim().isEmpty()) {
            module.setNom(moduleDTO.getNom());
        }
        if (moduleDTO.getDescription() != null) {
            module.setDescription(moduleDTO.getDescription());
        }
        if (moduleDTO.getSemestre() != null) {
            module.setSemestre(moduleDTO.getSemestre());
        }
        if (moduleDTO.getCoefficient() != null) {
            module.setCoefficient(moduleDTO.getCoefficient());
        }
        
        Module moduleModifie = moduleRepository.save(module);
        return structureMapper.versModuleDTO(moduleModifie);
    }
    
    @Override
    public void supprimerModule(Long moduleId) throws StructureAcademiqueException {
        if (moduleId == null) {
            throw new StructureAcademiqueException("L'identifiant du module est obligatoire");
        }
        
        Module module = moduleRepository.findById(moduleId).orElseThrow(() ->
                new StructureAcademiqueException("Module non trouvé avec l'identifiant: " + moduleId));
        
        // Vérifier les dépendances
        List<Matiere> matieres = matiereRepository.findByModuleId(moduleId);
        if (!matieres.isEmpty()) {
            throw new StructureAcademiqueException("Suppression impossible : le module contient des matières");
        }
        
        moduleRepository.delete(module);
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
                new StructureAcademiqueException("Module introuvable avec l'identifiant: " + matiereDTO.getModuleId()));

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
            throw new StructureAcademiqueException("L'identifiant du module est obligatoire");
        }
        
        try {
            List<Matiere> matieres = matiereRepository.findByModuleId(moduleId);
            return structureMapper.versListeMatieresDTO(matieres);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la recherche des matières par module: " + e.getMessage(), e);
        }
    }

    @Override
    public MatiereDTO modifierMatiere(Long matiereId, MatiereDTO matiereDTO) throws StructureAcademiqueException {
        if (matiereId == null) {
            throw new StructureAcademiqueException("L'identifiant de la matière est obligatoire");
        }
        
        if (matiereDTO == null) {
            throw new StructureAcademiqueException("La matière ne peut pas être nulle");
        }
        
        // Charger l'entité existante
        Matiere matiere = matiereRepository.findById(matiereId).orElseThrow(() ->
                new StructureAcademiqueException("Matière non trouvée avec l'identifiant: " + matiereId));
        
        // Valider l'unicité du code si modifié
        if (matiereDTO.getCodeMatiere() != null && 
            !matiereDTO.getCodeMatiere().equals(matiere.getCodeMatiere()) &&
            matiereRepository.existsByCodeMatiere(matiereDTO.getCodeMatiere())) {
            throw new StructureAcademiqueException("Une matière avec ce code existe déjà");
        }
        
        // Valider le module si modifié
        if (matiereDTO.getModuleId() != null && !matiereDTO.getModuleId().equals(matiere.getModule().getId())) {
            Module module = moduleRepository.findById(matiereDTO.getModuleId()).orElseThrow(() ->
                    new StructureAcademiqueException("Module introuvable avec l'identifiant: " + matiereDTO.getModuleId()));
            matiere.setModule(module);
        }
        
        // Valider coefficient si modifié
        if (matiereDTO.getCoefficient() != null && matiereDTO.getCoefficient() <= 0) {
            throw new StructureAcademiqueException("Le coefficient doit être supérieur à 0");
        }
        
        // Mettre à jour les champs autorisés
        if (matiereDTO.getCodeMatiere() != null && !matiereDTO.getCodeMatiere().trim().isEmpty()) {
            matiere.setCodeMatiere(matiereDTO.getCodeMatiere());
        }
        if (matiereDTO.getNom() != null && !matiereDTO.getNom().trim().isEmpty()) {
            matiere.setNom(matiereDTO.getNom());
        }
        if (matiereDTO.getDescription() != null) {
            matiere.setDescription(matiereDTO.getDescription());
        }
        if (matiereDTO.getCoefficient() != null) {
            matiere.setCoefficient(matiereDTO.getCoefficient());
        }
        
        Matiere matiereModifiee = matiereRepository.save(matiere);
        return structureMapper.versMatiereDTO(matiereModifiee);
    }
    
    @Override
    public void supprimerMatiere(Long matiereId) throws StructureAcademiqueException {
        if (matiereId == null) {
            throw new StructureAcademiqueException("L'identifiant de la matière est obligatoire");
        }
        
        Matiere matiere = matiereRepository.findById(matiereId).orElseThrow(() ->
                new StructureAcademiqueException("Matière non trouvée avec l'identifiant: " + matiereId));
        
        // Vérifier les dépendances
        List<AffectationEnseignement> affectations = affectationEnseignementRepository.findByMatiereId(matiereId);
        if (!affectations.isEmpty()) {
            throw new StructureAcademiqueException("Suppression impossible : la matière est utilisée dans des affectations d'enseignement");
        }
        
        List<Examen> examens = examenRepository.findByMatiereId(matiereId);
        if (!examens.isEmpty()) {
            throw new StructureAcademiqueException("Suppression impossible : la matière est utilisée dans des examens");
        }
        
        matiereRepository.delete(matiere);
    }
}
