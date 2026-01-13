package com.mycampus.backend.metier.administrateur.structureAcademique;

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
import com.mycampus.backend.entites.*;
import com.mycampus.backend.entites.Module;
import com.mycampus.backend.repositorys.*;
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
    private final ProfesseurRepository professeurRepository;

    // Département management
    @Override
    public DepartementSortieDTO creerDepartement(DepartementEntrerDTO departementEntrerDTO) throws StructureAcademiqueException {
        if (departementEntrerDTO == null) {
            throw new StructureAcademiqueException("Le département ne peut pas être nul");
        }
        
        if (departementEntrerDTO.getCodeDepartement() == null || departementEntrerDTO.getCodeDepartement().trim().isEmpty()) {
            throw new StructureAcademiqueException("Le code du département est obligatoire");
        }
        
        if (departementRepository.existsByCodeDepartement(departementEntrerDTO.getCodeDepartement())) {
            throw new StructureAcademiqueException("Un département avec ce code existe déjà");
        }

        
        Departement departement = structureMapper.FromDepartementEntrerVersDepartement(departementEntrerDTO);
        departement.setDateCreation( LocalDate.now());
        Departement departementSauvegarde = departementRepository.save(departement);
        return structureMapper.FromDepartementVersDepartementSortieDTO(departementSauvegarde);
    }

    @Override
    public DepartementSortieDTO trouverDepartementParId(Long id) throws StructureAcademiqueException {
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
        return structureMapper.FromDepartementVersDepartementSortieDTO(departement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartementSortieDTO> listerTousLesDepartements() throws StructureAcademiqueException {
        try {
            List<Departement> departements = departementRepository.findAll();
            return structureMapper.FromListeDepartementVersListeDepartementSortieDTO(departements);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la liste des départements: " + e.getMessage(), e);
        }
    }

    @Override
    public DepartementSortieDTO modifierDepartement(Long departementId, DepartementEntrerDTO departementEntrerDTO) throws StructureAcademiqueException {
        if (departementId == null) {
            throw new StructureAcademiqueException("L'identifiant du département est obligatoire");
        }
        
        if (departementEntrerDTO == null) {
            throw new StructureAcademiqueException("Le département ne peut pas être nul");
        }
        
        // Charger l'entité existante
        Departement departement = departementRepository.findById(departementId).orElseThrow(() ->
                new StructureAcademiqueException("Département non trouvé avec l'identifiant: " + departementId));
        
        // Valider l'unicité du code si modifié
        if (departementEntrerDTO.getCodeDepartement() != null &&
            !departementEntrerDTO.getCodeDepartement().equals(departement.getCodeDepartement()) &&
            departementRepository.existsByCodeDepartement(departementEntrerDTO.getCodeDepartement())) {
            throw new StructureAcademiqueException("Un département avec ce code existe déjà");
        }
        
        // Mettre à jour les champs autorisés
        if (departementEntrerDTO.getCodeDepartement() != null && !departementEntrerDTO.getCodeDepartement().trim().isEmpty()) {
            departement.setCodeDepartement(departementEntrerDTO.getCodeDepartement());
        }
        if (departementEntrerDTO.getTypeDepartement() != null && !departementEntrerDTO.getTypeDepartement().trim().isEmpty()) {
            departement.setTypeDepartement(departementEntrerDTO.getTypeDepartement());
        }
        if (departementEntrerDTO.getDescription() != null) {
            departement.setDescription(departementEntrerDTO.getDescription());
        }
        
        Departement departementModifie = departementRepository.save(departement);
        return structureMapper.FromDepartementVersDepartementSortieDTO(departementModifie);
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
    public FiliereSortieDTO creerFiliere(FiliereEntrerDTO filiereEntrerDTO) throws StructureAcademiqueException {
        if (filiereEntrerDTO == null) {
            throw new StructureAcademiqueException("La filière ne peut pas être nulle");
        }
        
        if (filiereEntrerDTO.getCodeFiliere() == null || filiereEntrerDTO.getCodeFiliere().trim().isEmpty()) {
            throw new StructureAcademiqueException("Le code de la filière est obligatoire");
        }
        
        if (filiereRepository.existsByCodeFiliere(filiereEntrerDTO.getCodeFiliere())) {
            throw new StructureAcademiqueException("Une filière avec ce code existe déjà");
        }
        
        if (filiereEntrerDTO.getDepartementId() == null) {
            throw new StructureAcademiqueException("La filière doit être rattachée à un département");
        }
        
        Filiere filiere = structureMapper.FromFiliereEntrerVersFiliere(filiereEntrerDTO);
        
        // Récupérer le département
        Departement departement = departementRepository.findById(filiereEntrerDTO.getDepartementId()).orElseThrow(() ->
                new StructureAcademiqueException("Département introuvable avec l'identifiant: " + filiereEntrerDTO.getDepartementId()));

        filiere.setDepartement(departement);
        
        Filiere filiereSauvegardee = filiereRepository.save(filiere);
        return structureMapper.FromFiliereVersFiliereSortieDTO(filiereSauvegardee);
    }

    @Override
    @Transactional(readOnly = true)
    public FiliereSortieDTO trouverFiliereParId(Long id) throws StructureAcademiqueException {
        if (id == null) {
            throw  new StructureAcademiqueException(
                    "id est obligatoire"
            );
        }
        
        Filiere filiere = filiereRepository.findById(id).orElseThrow(() ->
                new StructureAcademiqueException("filière non trouvé avec l'identifiant: " + id ));
        return structureMapper.FromFiliereVersFiliereSortieDTO(filiere);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FiliereSortieDTO> listerToutesLesFilieres() throws StructureAcademiqueException {
        try {
            List<Filiere> filieres = filiereRepository.findAll();
            return structureMapper.FromListeFiliereVersListeFiliereSortieDTO(filieres);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la liste des filières: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FiliereSortieDTO> trouverFilieresParDepartement(Long departementId) throws StructureAcademiqueException {
        if (departementId == null) {
            throw new StructureAcademiqueException("L'identifiant du département est obligatoire");
        }
        
        try {
            List<Filiere> filieres = filiereRepository.findByDepartementId(departementId);
            return structureMapper.FromListeFiliereVersListeFiliereSortieDTO(filieres);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la recherche des filières par département: " + e.getMessage(), e);
        }
    }

    @Override
    public FiliereSortieDTO modifierFiliere(Long filiereId, FiliereEntrerDTO filiereEntrerDTO) throws StructureAcademiqueException {
        if (filiereId == null) {
            throw new StructureAcademiqueException("L'identifiant de la filière est obligatoire");
        }
        
        if (filiereEntrerDTO == null) {
            throw new StructureAcademiqueException("La filière ne peut pas être nulle");
        }
        
        // Charger l'entité existante
        Filiere filiere = filiereRepository.findById(filiereId).orElseThrow(() ->
                new StructureAcademiqueException("Filière non trouvée avec l'identifiant: " + filiereId));
        
        // Valider l'unicité du code si modifié
        if (filiereEntrerDTO.getCodeFiliere() != null &&
            !filiereEntrerDTO.getCodeFiliere().equals(filiere.getCodeFiliere()) &&
            filiereRepository.existsByCodeFiliere(filiereEntrerDTO.getCodeFiliere())) {
            throw new StructureAcademiqueException("Une filière avec ce code existe déjà");
        }
        
        // Valider le département si modifié
        if (filiereEntrerDTO.getDepartementId() != null && !filiereEntrerDTO.getDepartementId().equals(filiere.getDepartement().getId())) {
            Departement departement = departementRepository.findById(filiereEntrerDTO.getDepartementId()).orElseThrow(() ->
                    new StructureAcademiqueException("Département introuvable avec l'identifiant: " + filiereEntrerDTO.getDepartementId()));
            filiere.setDepartement(departement);
        }
        
        // Mettre à jour les champs autorisés
        if (filiereEntrerDTO.getCodeFiliere() != null && !filiereEntrerDTO.getCodeFiliere().trim().isEmpty()) {
            filiere.setCodeFiliere(filiereEntrerDTO.getCodeFiliere());
        }
        if (filiereEntrerDTO.getNom() != null && !filiereEntrerDTO.getNom().trim().isEmpty()) {
            filiere.setNom(filiereEntrerDTO.getNom());
        }
        if (filiereEntrerDTO.getDescription() != null) {
            filiere.setDescription(filiereEntrerDTO.getDescription());
        }
        if (filiereEntrerDTO.getEmploiTemps() != null) {
            filiere.setEmploiTemps(filiereEntrerDTO.getEmploiTemps());
        }
        if (filiereEntrerDTO.getDureeFormation() != null && !filiereEntrerDTO.getDureeFormation().trim().isEmpty()) {
            filiere.setDureeFormation(filiereEntrerDTO.getDureeFormation());
        }
        if (filiereEntrerDTO.getNiveauDiplome() != null && !filiereEntrerDTO.getNiveauDiplome().trim().isEmpty()) {
            filiere.setNiveauDiplome(filiereEntrerDTO.getNiveauDiplome());
        }
        if (filiereEntrerDTO.getEtat() != null) {
            filiere.setEtat(filiereEntrerDTO.getEtat());
        }
        
        Filiere filiereModifiee = filiereRepository.save(filiere);
        return structureMapper.FromFiliereVersFiliereSortieDTO(filiereModifiee);
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
    public ClasseSortieDTO creerClasse(ClasseEntrerDTO classeEntrerDTO) throws StructureAcademiqueException {
        if (classeEntrerDTO == null) {
            throw new StructureAcademiqueException("La classe ne peut pas être nulle");
        }
        
        if (classeEntrerDTO.getCodeClasse() == null || classeEntrerDTO.getCodeClasse().trim().isEmpty()) {
            throw new StructureAcademiqueException("Le code de la classe est obligatoire");
        }
        
        if (classeRepository.existsByCodeClasse(classeEntrerDTO.getCodeClasse())) {
            throw new StructureAcademiqueException("Une classe avec ce code existe déjà");
        }
        
        if (classeEntrerDTO.getFiliereId() == null) {
            throw new StructureAcademiqueException("La classe doit être rattachée à une filière");
        }
        
        if (classeEntrerDTO.getEffectifMax() == null || classeEntrerDTO.getEffectifMax() <= 0) {
            throw new StructureAcademiqueException("L'effectif maximum est obligatoire et doit être supérieur à 0");
        }
        
        Classe classe = structureMapper.FromClasseEntrerVersClasse(classeEntrerDTO);
        
        // Récupérer la filière
        Filiere filiere = filiereRepository.findById(classeEntrerDTO.getFiliereId()).orElseThrow(() ->
             new StructureAcademiqueException("Filière introuvable avec l'identifiant: " + classeEntrerDTO.getFiliereId()));

        classe.setFiliere(filiere);
        
        Classe classeSauvegardee = classeRepository.save(classe);
        return structureMapper.FromClasseVersClasseSortieDTO(classeSauvegardee);
    }

    @Override
    public ClasseSortieDTO trouverClasseParId(Long id) throws StructureAcademiqueException {
        if (id == null) {
            throw  new StructureAcademiqueException(
                    "id est obligatoire"
            );
        }
        
        Classe classe = classeRepository.findById(id).orElseThrow(() ->
                new StructureAcademiqueException("Classe non trouvé avec l'identifiant: " + id));;
        return structureMapper.FromClasseVersClasseSortieDTO(classe);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClasseSortieDTO> listerToutesLesClasses() throws StructureAcademiqueException {
        try {
            List<Classe> classes = classeRepository.findAll();
            return structureMapper.FromListeClasseVersListeClasseSortieDTO(classes);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la liste des classes: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClasseSortieDTO> trouverClassesParFiliere(Long filiereId) throws StructureAcademiqueException {
        if (filiereId == null) {
            throw new StructureAcademiqueException("L'identifiant de la filière est obligatoire");
        }
        
        try {
            List<Classe> classes = classeRepository.findByFiliereId(filiereId);
            return structureMapper.FromListeClasseVersListeClasseSortieDTO(classes);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la recherche des classes par filière: " + e.getMessage(), e);
        }
    }

    @Override
    public ClasseSortieDTO modifierClasse(Long classeId, ClasseEntrerDTO classeEntrerDTO) throws StructureAcademiqueException {
        if (classeId == null) {
            throw new StructureAcademiqueException("L'identifiant de la classe est obligatoire");
        }
        
        if (classeEntrerDTO == null) {
            throw new StructureAcademiqueException("La classe ne peut pas être nulle");
        }
        
        // Charger l'entité existante
        Classe classe = classeRepository.findById(classeId).orElseThrow(() ->
                new StructureAcademiqueException("Classe non trouvée avec l'identifiant: " + classeId));
        
        // Valider l'unicité du code si modifié
        if (classeEntrerDTO.getCodeClasse() != null &&
            !classeEntrerDTO.getCodeClasse().equals(classe.getCodeClasse()) &&
            classeRepository.existsByCodeClasse(classeEntrerDTO.getCodeClasse())) {
            throw new StructureAcademiqueException("Une classe avec ce code existe déjà");
        }
        
        // Valider la filière si modifiée
        if (classeEntrerDTO.getFiliereId() != null && !classeEntrerDTO.getFiliereId().equals(classe.getFiliere().getId())) {
            Filiere filiere = filiereRepository.findById(classeEntrerDTO.getFiliereId()).orElseThrow(() ->
                    new StructureAcademiqueException("Filière introuvable avec l'identifiant: " + classeEntrerDTO.getFiliereId()));
            classe.setFiliere(filiere);
        }
        
        // Valider effectifMax si modifié
        if (classeEntrerDTO.getEffectifMax() != null && classeEntrerDTO.getEffectifMax() <= 0) {
            throw new StructureAcademiqueException("L'effectif maximum doit être supérieur à 0");
        }
        
        // Mettre à jour les champs autorisés
        if (classeEntrerDTO.getCodeClasse() != null && !classeEntrerDTO.getCodeClasse().trim().isEmpty()) {
            classe.setCodeClasse(classeEntrerDTO.getCodeClasse());
        }
        if (classeEntrerDTO.getNom() != null && !classeEntrerDTO.getNom().trim().isEmpty()) {
            classe.setNom(classeEntrerDTO.getNom());
        }
        if (classeEntrerDTO.getNiveau() != null && !classeEntrerDTO.getNiveau().trim().isEmpty()) {
            classe.setNiveau(classeEntrerDTO.getNiveau());
        }
        if (classeEntrerDTO.getAnneeUniversitaire() != null && !classeEntrerDTO.getAnneeUniversitaire().trim().isEmpty()) {
            classe.setAnneeUniversitaire(classeEntrerDTO.getAnneeUniversitaire());
        }
        if (classeEntrerDTO.getEffectifMax() != null) {
            classe.setEffectifMax(classeEntrerDTO.getEffectifMax());
        }
        
        Classe classeModifiee = classeRepository.save(classe);
        return structureMapper.FromClasseVersClasseSortieDTO(classeModifiee);
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
    public ModuleSortieDTO creerModule(ModuleEntrerDTO moduleEntrerDTO) throws StructureAcademiqueException {
        if (moduleEntrerDTO == null) {
            throw new StructureAcademiqueException("Le module ne peut pas être nul");
        }
        
        if (moduleEntrerDTO.getCodeModule() == null || moduleEntrerDTO.getCodeModule().trim().isEmpty()) {
            throw new StructureAcademiqueException("Le code du module est obligatoire");
        }
        
        if (moduleRepository.existsByCodeModule(moduleEntrerDTO.getCodeModule())) {
            throw new StructureAcademiqueException("Un module avec ce code existe déjà");
        }
        
        if (moduleEntrerDTO.getFiliereId() == null) {
            throw new StructureAcademiqueException("Le module doit être rattaché à une filière");
        }
        
        Module module = structureMapper.FromModuleEntrerVersModule(moduleEntrerDTO);
        
        // Récupérer la filière
        Filiere filiere = filiereRepository.findById(moduleEntrerDTO.getFiliereId()).orElseThrow(() ->
            new StructureAcademiqueException("Filière introuvable avec l'identifiant: " + moduleEntrerDTO.getFiliereId()));

        module.setFiliere(filiere);
        
        Module moduleSauvegarde = moduleRepository.save(module);
        return structureMapper.FromModuleVersModuleSortieDTO(moduleSauvegarde);
    }

    @Override
    public ModuleSortieDTO trouverModuleParId(Long id) throws StructureAcademiqueException {
        if (id == null) {
            throw  new StructureAcademiqueException(
                    "id est obligatoire"
            );
        }
        
        Module module = moduleRepository.findById(id).orElseThrow(() ->
                new StructureAcademiqueException("Module non trouvé avec l'identifiant: " + id));;;
        return structureMapper.FromModuleVersModuleSortieDTO(module);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleSortieDTO> listerTousLesModules() throws StructureAcademiqueException {
        try {
            List<Module> modules = moduleRepository.findAll();
            return structureMapper.FromListeModuleVersListeModuleSortieDTO(modules);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la liste des modules: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleSortieDTO> trouverModulesParFiliere(Long filiereId) throws StructureAcademiqueException {
        if (filiereId == null) {
            throw new StructureAcademiqueException("L'identifiant de la filière est obligatoire");
        }
        
        try {
            List<Module> modules = moduleRepository.findByFiliereId(filiereId);
            return structureMapper.FromListeModuleVersListeModuleSortieDTO(modules);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la recherche des modules par filière: " + e.getMessage(), e);
        }
    }

    @Override
    public ModuleSortieDTO modifierModule(Long moduleId, ModuleEntrerDTO moduleEntrerDTO) throws StructureAcademiqueException {
        if (moduleId == null) {
            throw new StructureAcademiqueException("L'identifiant du module est obligatoire");
        }
        
        if (moduleEntrerDTO == null) {
            throw new StructureAcademiqueException("Le module ne peut pas être nul");
        }
        
        // Charger l'entité existante
        Module module = moduleRepository.findById(moduleId).orElseThrow(() ->
                new StructureAcademiqueException("Module non trouvé avec l'identifiant: " + moduleId));
        
        // Valider l'unicité du code si modifié
        if (moduleEntrerDTO.getCodeModule() != null &&
            !moduleEntrerDTO.getCodeModule().equals(module.getCodeModule()) &&
            moduleRepository.existsByCodeModule(moduleEntrerDTO.getCodeModule())) {
            throw new StructureAcademiqueException("Un module avec ce code existe déjà");
        }
        
        // Valider la filière si modifiée
        if (moduleEntrerDTO.getFiliereId() != null && !moduleEntrerDTO.getFiliereId().equals(module.getFiliere().getId())) {
            Filiere filiere = filiereRepository.findById(moduleEntrerDTO.getFiliereId()).orElseThrow(() ->
                    new StructureAcademiqueException("Filière non trouvée avec l'identifiant: " + moduleEntrerDTO.getFiliereId()));
            module.setFiliere(filiere);
        }
        
        // Valider coefficient si modifié
        if (moduleEntrerDTO.getCoefficient() != null && moduleEntrerDTO.getCoefficient() <= 0) {
            throw new StructureAcademiqueException("Le coefficient doit être supérieur à 0");
        }
        
        // Mettre à jour les champs autorisés
        if (moduleEntrerDTO.getCodeModule() != null && !moduleEntrerDTO.getCodeModule().trim().isEmpty()) {
            module.setCodeModule(moduleEntrerDTO.getCodeModule());
        }
        if (moduleEntrerDTO.getNom() != null && !moduleEntrerDTO.getNom().trim().isEmpty()) {
            module.setNom(moduleEntrerDTO.getNom());
        }
        if (moduleEntrerDTO.getDescription() != null) {
            module.setDescription(moduleEntrerDTO.getDescription());
        }
        if (moduleEntrerDTO.getSemestre() != null) {
            module.setSemestre(moduleEntrerDTO.getSemestre());
        }
        if (moduleEntrerDTO.getCoefficient() != null) {
            module.setCoefficient(moduleEntrerDTO.getCoefficient());
        }
        
        Module moduleModifie = moduleRepository.save(module);
        return structureMapper.FromModuleVersModuleSortieDTO(moduleModifie);
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
    public MatiereSortieDTO creerMatiere(MatiereEntrerDTO matiereEntrerDTO) throws StructureAcademiqueException {
        if (matiereEntrerDTO == null) {
            throw new StructureAcademiqueException("La matière ne peut pas être nulle");
        }
        
        if (matiereEntrerDTO.getCodeMatiere() == null || matiereEntrerDTO.getCodeMatiere().trim().isEmpty()) {
            throw new StructureAcademiqueException("Le code de la matière est obligatoire");
        }
        
        if (matiereRepository.existsByCodeMatiere(matiereEntrerDTO.getCodeMatiere())) {
            throw new StructureAcademiqueException("Une matière avec ce code existe déjà");
        }
        
        if (matiereEntrerDTO.getModuleId() == null) {
            throw new StructureAcademiqueException("La matière doit être rattachée à un module");
        }
        
        Matiere matiere = structureMapper.FromMatiereEntrerVersMatiere(matiereEntrerDTO);
        
        // Récupérer le module
        Module module = moduleRepository.findById(matiereEntrerDTO.getModuleId()).orElseThrow(() ->
                new StructureAcademiqueException("Module introuvable avec l'identifiant: " + matiereEntrerDTO.getModuleId()));

        matiere.setModule(module);
        
        Matiere matiereSauvegardee = matiereRepository.save(matiere);
        return structureMapper.FromMatiereVersMatiereSortieDTO(matiereSauvegardee);
    }

    @Override
    public MatiereSortieDTO trouverMatiereParId(Long id) throws StructureAcademiqueException {
        if (id == null) {
            throw  new StructureAcademiqueException(
                    "id est obligatoire"
            );
        }
        
        Matiere matiere = matiereRepository.findById(id).orElseThrow(() ->
                new StructureAcademiqueException("Matière non trouvé avec l'identifiant: " + id));;
        return structureMapper.FromMatiereVersMatiereSortieDTO(matiere);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatiereSortieDTO> listerToutesLesMatieres() throws StructureAcademiqueException {
        try {
            List<Matiere> matieres = matiereRepository.findAll();
            return structureMapper.FromListeMatiereVersListeMatiereSortieDTO(matieres);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la liste des matières: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatiereSortieDTO> trouverMatieresParModule(Long moduleId) throws StructureAcademiqueException {
        if (moduleId == null) {
            throw new StructureAcademiqueException("L'identifiant du module est obligatoire");
        }
        
        try {
            List<Matiere> matieres = matiereRepository.findByModuleId(moduleId);
            return structureMapper.FromListeMatiereVersListeMatiereSortieDTO(matieres);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la recherche des matières par module: " + e.getMessage(), e);
        }
    }

    @Override
    public MatiereSortieDTO modifierMatiere(Long matiereId, MatiereEntrerDTO matiereEntrerDTO) throws StructureAcademiqueException {
        if (matiereId == null) {
            throw new StructureAcademiqueException("L'identifiant de la matière est obligatoire");
        }
        
        if (matiereEntrerDTO == null) {
            throw new StructureAcademiqueException("La matière ne peut pas être nulle");
        }
        
        // Charger l'entité existante
        Matiere matiere = matiereRepository.findById(matiereId).orElseThrow(() ->
                new StructureAcademiqueException("Matière non trouvée avec l'identifiant: " + matiereId));
        
        // Valider l'unicité du code si modifié
        if (matiereEntrerDTO.getCodeMatiere() != null &&
            !matiereEntrerDTO.getCodeMatiere().equals(matiere.getCodeMatiere()) &&
            matiereRepository.existsByCodeMatiere(matiereEntrerDTO.getCodeMatiere())) {
            throw new StructureAcademiqueException("Une matière avec ce code existe déjà");
        }
        
        // Valider le module si modifié
        if (matiereEntrerDTO.getModuleId() != null && !matiereEntrerDTO.getModuleId().equals(matiere.getModule().getId())) {
            Module module = moduleRepository.findById(matiereEntrerDTO.getModuleId()).orElseThrow(() ->
                    new StructureAcademiqueException("Module introuvable avec l'identifiant: " + matiereEntrerDTO.getModuleId()));
            matiere.setModule(module);
        }
        
        // Valider coefficient si modifié
        if (matiereEntrerDTO.getCoefficient() != null && matiereEntrerDTO.getCoefficient() <= 0) {
            throw new StructureAcademiqueException("Le coefficient doit être supérieur à 0");
        }
        
        // Mettre à jour les champs autorisés
        if (matiereEntrerDTO.getCodeMatiere() != null && !matiereEntrerDTO.getCodeMatiere().trim().isEmpty()) {
            matiere.setCodeMatiere(matiereEntrerDTO.getCodeMatiere());
        }
        if (matiereEntrerDTO.getNom() != null && !matiereEntrerDTO.getNom().trim().isEmpty()) {
            matiere.setNom(matiereEntrerDTO.getNom());
        }
        if (matiereEntrerDTO.getDescription() != null) {
            matiere.setDescription(matiereEntrerDTO.getDescription());
        }
        if (matiereEntrerDTO.getCoefficient() != null) {
            matiere.setCoefficient(matiereEntrerDTO.getCoefficient());
        }
        
        Matiere matiereModifiee = matiereRepository.save(matiere);
        return structureMapper.FromMatiereVersMatiereSortieDTO(matiereModifiee);
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

    @Override
    public AffectationEnseignementSortieDTO creerAffectationEnseignement(AffectationEnseignementEntrerDTO affectationEnseignementEntrerDTO) throws StructureAcademiqueException {
        if(affectationEnseignementEntrerDTO == null) {
            throw new StructureAcademiqueException("l'affectation ne peut pas être null ");
        }

        if (affectationEnseignementEntrerDTO.getAnneeUniversitaire() == null || affectationEnseignementEntrerDTO.getAnneeUniversitaire().trim().isEmpty()) {
            throw new StructureAcademiqueException("Année Universitaire est obligatoire");
        }
        if(affectationEnseignementEntrerDTO.getSemestre() == null){
            throw new StructureAcademiqueException("Semestre est obligatoire");
        }

        if(affectationEnseignementEntrerDTO.getProfesseurPpr() == null || affectationEnseignementEntrerDTO.getProfesseurPpr().trim().isEmpty()) {
            throw new StructureAcademiqueException("Professeur est obligatoire");

        }
        if(!professeurRepository.existsByPpr(affectationEnseignementEntrerDTO.getProfesseurPpr())) {
            throw new StructureAcademiqueException("Professeur n'existe pas");
        }
        if (affectationEnseignementEntrerDTO.getClasseId() == null) {
            throw new StructureAcademiqueException("Classe est obligatoire");
        }

        if(!classeRepository.existsById(affectationEnseignementEntrerDTO.getClasseId())) {
            throw new StructureAcademiqueException("Classe n'existe pas");
        }

        if (affectationEnseignementEntrerDTO.getMatiereId() == null) {
            throw new StructureAcademiqueException("Matiere est obligatoire");
        }

        if(!matiereRepository.existsById(affectationEnseignementEntrerDTO.getMatiereId())) {
            throw new StructureAcademiqueException("Matiere n'existe pas");
        }

        AffectationEnseignement affectationEnseignement = structureMapper.FromAffectationEnseignementEntrerVersAffectationEnseignement(affectationEnseignementEntrerDTO);
        affectationEnseignement.setActive(true);

        affectationEnseignement.setProfesseur(professeurRepository.findByPpr(affectationEnseignementEntrerDTO.getProfesseurPpr()));
        affectationEnseignement.setClasse(classeRepository.findById(affectationEnseignementEntrerDTO.getClasseId()).get());
        affectationEnseignement.setMatiere(matiereRepository.findById(affectationEnseignementEntrerDTO.getMatiereId()).get());


        return structureMapper.FromAffectationEnseignementVersAffectationEnseignementSortieDTO(affectationEnseignementRepository.save(affectationEnseignement));

    }

    @Override
    public AffectationEnseignementSortieDTO modifierAffectationEnseignement(Long id, AffectationEnseignementEntrerDTO affectationEnseignementEntrerDTO) throws StructureAcademiqueException {
        if (id == null) {
            throw new StructureAcademiqueException("L'identifiant de l'affectation est obligatoire");
        }
        
        if (affectationEnseignementEntrerDTO == null) {
            throw new StructureAcademiqueException("L'affectation ne peut pas être nulle");
        }
        
        AffectationEnseignement affectationExistante = affectationEnseignementRepository.findById(id).orElseThrow(() ->
                new StructureAcademiqueException("Affectation introuvable avec l'identifiant: " + id));
        
        // Mise à jour des champs autorisés
        if (affectationEnseignementEntrerDTO.getAnneeUniversitaire() != null && !affectationEnseignementEntrerDTO.getAnneeUniversitaire().trim().isEmpty()) {
            affectationExistante.setAnneeUniversitaire(affectationEnseignementEntrerDTO.getAnneeUniversitaire());
        }
        
        if (affectationEnseignementEntrerDTO.getSemestre() != null) {
            affectationExistante.setSemestre(affectationEnseignementEntrerDTO.getSemestre());
        }
        
        // Mise à jour du professeur si fourni et différent
        if (affectationEnseignementEntrerDTO.getProfesseurPpr() != null &&
            (affectationExistante.getProfesseur() == null || !affectationEnseignementEntrerDTO.getProfesseurPpr().equals(affectationExistante.getProfesseur().getPpr()))) {
            
            Professeur professeur = professeurRepository.findByPpr(affectationEnseignementEntrerDTO.getProfesseurPpr());
            if (professeur == null) {
                throw new StructureAcademiqueException("Professeur introuvable avec le PPR: " + affectationEnseignementEntrerDTO.getProfesseurPpr());
            }
            affectationExistante.setProfesseur(professeur);
        }
        
        // Mise à jour de la classe si fournie et différente
        if (affectationEnseignementEntrerDTO.getClasseId() != null &&
            (affectationExistante.getClasse() == null || !affectationEnseignementEntrerDTO.getClasseId().equals(affectationExistante.getClasse().getId()))) {
            
            Classe classe = classeRepository.findById(affectationEnseignementEntrerDTO.getClasseId()).orElseThrow(() ->
                    new StructureAcademiqueException("Classe introuvable avec l'identifiant: " + affectationEnseignementEntrerDTO.getClasseId()));
            affectationExistante.setClasse(classe);
        }
        
        // Mise à jour de la matière si fournie et différente
        if (affectationEnseignementEntrerDTO.getMatiereId() != null &&
            (affectationExistante.getMatiere() == null || !affectationEnseignementEntrerDTO.getMatiereId().equals(affectationExistante.getMatiere().getId()))) {
            
            Matiere matiere = matiereRepository.findById(affectationEnseignementEntrerDTO.getMatiereId()).orElseThrow(() ->
                    new StructureAcademiqueException("Matière introuvable avec l'identifiant: " + affectationEnseignementEntrerDTO.getMatiereId()));
            affectationExistante.setMatiere(matiere);
        }
        
        // Mise à jour du statut actif si fourni
        if (affectationEnseignementEntrerDTO.getActive() != null) {
            affectationExistante.setActive(affectationEnseignementEntrerDTO.getActive());
        }
        
        return structureMapper.FromAffectationEnseignementVersAffectationEnseignementSortieDTO(affectationEnseignementRepository.save(affectationExistante));
    }

    @Override
    public void activerAffectationEnseignement(Long id) throws StructureAcademiqueException {
        if (id == null) {
            throw new StructureAcademiqueException("L'identifiant de l'affectation est obligatoire");
        }
        
        AffectationEnseignement affectation = affectationEnseignementRepository.findById(id).orElseThrow(() ->
                new StructureAcademiqueException("Affectation introuvable avec l'identifiant: " + id));
        
        affectation.setActive(true);
        affectationEnseignementRepository.save(affectation);
    }

    @Override
    public void desactiverAffectationEnseignement(Long id) throws StructureAcademiqueException {
        if (id == null) {
            throw new StructureAcademiqueException("L'identifiant de l'affectation est obligatoire");
        }
        
        AffectationEnseignement affectation = affectationEnseignementRepository.findById(id).orElseThrow(() ->
                new StructureAcademiqueException("Affectation introuvable avec l'identifiant: " + id));
        
        affectation.setActive(false);
        affectationEnseignementRepository.save(affectation);
    }

    @Override
    public AffectationEnseignementSortieDTO trouverAffectationParId(Long id) throws StructureAcademiqueException {
        if (id == null) {
            throw new StructureAcademiqueException("L'identifiant de l'affectation est obligatoire");
        }
        
        AffectationEnseignement affectation = affectationEnseignementRepository.findById(id).orElseThrow(() ->
                new StructureAcademiqueException("Affectation introuvable avec l'identifiant: " + id));
        
        return structureMapper.FromAffectationEnseignementVersAffectationEnseignementSortieDTO(affectation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AffectationEnseignementSortieDTO> listerToutesLesAffectations() throws StructureAcademiqueException {
        try {
            List<AffectationEnseignement> affectations = affectationEnseignementRepository.findAll();
            return structureMapper.FromListeAffectationEnseignementVersListeAffectationEnseignementSortieDTO(affectations);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la liste des affectations: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AffectationEnseignementSortieDTO> listerAffectationsParProfesseur(String professeurPpr) throws StructureAcademiqueException {
        if (professeurPpr == null || professeurPpr.trim().isEmpty()) {
            throw new StructureAcademiqueException("Le PPR du professeur est obligatoire");
        }
        
        if (!professeurRepository.existsByPpr(professeurPpr)) {
            throw new StructureAcademiqueException("Professeur introuvable avec le PPR: " + professeurPpr);
        }
        
        try {
            List<AffectationEnseignement> affectations = affectationEnseignementRepository.findByProfesseur_Ppr(professeurPpr);
            return structureMapper.FromListeAffectationEnseignementVersListeAffectationEnseignementSortieDTO(affectations);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la recherche des affectations par professeur: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AffectationEnseignementSortieDTO> listerAffectationsParClasse(Long classeId) throws StructureAcademiqueException {
        if (classeId == null) {
            throw new StructureAcademiqueException("L'identifiant de la classe est obligatoire");
        }
        
        if (!classeRepository.existsById(classeId)) {
            throw new StructureAcademiqueException("Classe introuvable avec l'identifiant: " + classeId);
        }
        
        try {
            List<AffectationEnseignement> affectations = affectationEnseignementRepository.findByClasseId(classeId);
            return structureMapper.FromListeAffectationEnseignementVersListeAffectationEnseignementSortieDTO(affectations);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la recherche des affectations par classe: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AffectationEnseignementSortieDTO> listerAffectationsParMatiere(Long matiereId) throws StructureAcademiqueException {
        if (matiereId == null) {
            throw new StructureAcademiqueException("L'identifiant de la matière est obligatoire");
        }
        
        if (!matiereRepository.existsById(matiereId)) {
            throw new StructureAcademiqueException("Matière introuvable avec l'identifiant: " + matiereId);
        }
        
        try {
            List<AffectationEnseignement> affectations = affectationEnseignementRepository.findByMatiereId(matiereId);
            return structureMapper.FromListeAffectationEnseignementVersListeAffectationEnseignementSortieDTO(affectations);
        } catch (Exception e) {
            throw new StructureAcademiqueException("Erreur lors de la recherche des affectations par matière: " + e.getMessage(), e);
        }
    }

    @Override
    public void supprimerAffectationEnseignement(Long id) throws StructureAcademiqueException {
        if (id == null) {
            throw new StructureAcademiqueException("L'identifiant de l'affectation est obligatoire");
        }
        
        AffectationEnseignement affectation = affectationEnseignementRepository.findById(id).orElseThrow(() ->
                new StructureAcademiqueException("Affectation introuvable avec l'identifiant: " + id));
        
        affectationEnseignementRepository.delete(affectation);
    }
}
