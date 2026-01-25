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
    import com.mycampus.backend.mappers.administrateur.StructureMapper;
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

        // Méthodes utilitaires requireX
        private Departement requireDepartement(String codeDepartement) {
            if (codeDepartement == null || codeDepartement.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code du département est obligatoire");
            }
            return departementRepository.findByCodeDepartement(codeDepartement)
                    .orElseThrow(() -> new StructureAcademiqueException("Département non trouvé avec le code: " + codeDepartement));
        }

        private Filiere requireFiliere(String codeFiliere) {
            if (codeFiliere == null || codeFiliere.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la filière est obligatoire");
            }
            return filiereRepository.findByCodeFiliere(codeFiliere)
                    .orElseThrow(() -> new StructureAcademiqueException("Filière non trouvée avec le code: " + codeFiliere));
        }

        private Classe requireClasse(String codeClasse) {
            if (codeClasse == null || codeClasse.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la classe est obligatoire");
            }
            return classeRepository.findByCodeClasse(codeClasse)
                    .orElseThrow(() -> new StructureAcademiqueException("Classe non trouvée avec le code: " + codeClasse));
        }

        private Module requireModule(String codeModule) {
            if (codeModule == null || codeModule.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code du module est obligatoire");
            }
            return moduleRepository.findByCodeModule(codeModule)
                    .orElseThrow(() -> new StructureAcademiqueException("Module non trouvé avec le code: " + codeModule));
        }

        private Matiere requireMatiere(String codeMatiere) {
            if (codeMatiere == null || codeMatiere.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la matière est obligatoire");
            }
            return matiereRepository.findByCodeMatiere(codeMatiere)
                    .orElseThrow(() -> new StructureAcademiqueException("Matière non trouvée avec le code: " + codeMatiere));
        }

        private Professeur requireProfesseur(String ppr) {
            if (ppr == null || ppr.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le PPR du professeur est obligatoire");
            }
            return professeurRepository.findByPpr(ppr)
                    .orElseThrow(() -> new StructureAcademiqueException("Professeur non trouvé avec le PPR: " + ppr));
        }

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

            // Gérer le chef de département si fourni
            if (departementEntrerDTO.getChefDepartementPpr() != null && !departementEntrerDTO.getChefDepartementPpr().trim().isEmpty()) {
                Professeur chefDepartement = requireProfesseur(departementEntrerDTO.getChefDepartementPpr());
                departement.setChefDepartement(chefDepartement);
            }

            Departement departementSauvegarde = departementRepository.save(departement);
            return structureMapper.FromDepartementVersDepartementSortieDTO(departementSauvegarde);
        }

        @Override
        public DepartementSortieDTO trouverDepartementParCode(String codeDepartement) throws StructureAcademiqueException {
            if (codeDepartement == null || codeDepartement.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code du département est obligatoire");
            }

            Departement departement = requireDepartement(codeDepartement);
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
        public DepartementSortieDTO modifierDepartement(String codeDepartement, DepartementEntrerDTO departementEntrerDTO) throws StructureAcademiqueException {
            if (codeDepartement == null || codeDepartement.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code du département est obligatoire");
            }

            if (departementEntrerDTO == null) {
                throw new StructureAcademiqueException("Le département ne peut pas être nul");
            }

            Departement departement = requireDepartement(codeDepartement);

            // Gérer le chef de département si fourni
            if (departementEntrerDTO.getChefDepartementPpr() != null && !departementEntrerDTO.getChefDepartementPpr().trim().isEmpty()) {
                Professeur chefDepartement = requireProfesseur(departementEntrerDTO.getChefDepartementPpr());
                departement.setChefDepartement(chefDepartement);
            }

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
        public void supprimerDepartement(String codeDepartement) throws StructureAcademiqueException {
            if (codeDepartement == null || codeDepartement.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code du département est obligatoire");
            }

            Departement departement = requireDepartement(codeDepartement);

            // Vérifier les dépendances
            List<Filiere> filieres = filiereRepository.findByDepartement_CodeDepartement(codeDepartement);
            if (!filieres.isEmpty()) {
                throw new StructureAcademiqueException("Suppression impossible : le département contient des filières");
            }

            List<Professeur> professeurs = professeurRepository.findByDepartementDirigee_CodeDepartement(codeDepartement);
            if (!professeurs.isEmpty()) {
                throw new StructureAcademiqueException("Suppression impossible : le département a un chef assigné");
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

            if (filiereEntrerDTO.getCodeDepartement() == null || filiereEntrerDTO.getCodeDepartement().trim().isEmpty()) {
                throw new StructureAcademiqueException("La filière doit être rattachée à un département");
            }

            Filiere filiere = structureMapper.FromFiliereEntrerVersFiliere(filiereEntrerDTO);

            // Récupérer le département
            Departement departement = requireDepartement(filiereEntrerDTO.getCodeDepartement());

            filiere.setDepartement(departement);

            Filiere filiereSauvegardee = filiereRepository.save(filiere);
            return structureMapper.FromFiliereVersFiliereSortieDTO(filiereSauvegardee);
        }

        @Override
        public FiliereSortieDTO trouverFiliereParCode(String codeFiliere) throws StructureAcademiqueException {
            if (codeFiliere == null || codeFiliere.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la filière est obligatoire");
            }

            Filiere filiere = requireFiliere(codeFiliere);
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
        public List<FiliereSortieDTO> trouverFilieresParDepartement(String codeDepartement) throws StructureAcademiqueException {
            if (codeDepartement == null || codeDepartement.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code du département est obligatoire");
            }

            Departement departement = requireDepartement(codeDepartement);

            try {
                List<Filiere> filieres = filiereRepository.findByDepartement_CodeDepartement(codeDepartement);
                return structureMapper.FromListeFiliereVersListeFiliereSortieDTO(filieres);
            } catch (Exception e) {
                throw new StructureAcademiqueException("Erreur lors de la recherche des filières par département: " + e.getMessage(), e);
            }
        }

        @Override
        public FiliereSortieDTO modifierFiliere(String codeFiliere, FiliereEntrerDTO filiereEntrerDTO) throws StructureAcademiqueException {
            if (codeFiliere == null || codeFiliere.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la filière est obligatoire");
            }

            if (filiereEntrerDTO == null) {
                throw new StructureAcademiqueException("La filière ne peut pas être nulle");
            }

            Filiere filiere = requireFiliere(codeFiliere);

            // Gérer le chef de filière si fourni
            if (filiereEntrerDTO.getChefFilierePpr() != null && !filiereEntrerDTO.getChefFilierePpr().trim().isEmpty()) {
                Professeur chefFiliere = requireProfesseur(filiereEntrerDTO.getChefFilierePpr());
                filiere.setChefFiliere(chefFiliere);
            }

            // Valider l'unicité du code si modifié
            if (filiereEntrerDTO.getCodeFiliere() != null &&
                !filiereEntrerDTO.getCodeFiliere().equals(filiere.getCodeFiliere()) &&
                filiereRepository.existsByCodeFiliere(filiereEntrerDTO.getCodeFiliere())) {
                throw new StructureAcademiqueException("Une filière avec ce code existe déjà");
            }

            // Valider le département si modifié
            if (filiereEntrerDTO.getCodeDepartement() != null && !filiereEntrerDTO.getCodeDepartement().equals(filiere.getDepartement().getCodeDepartement())) {
                Departement departement = requireDepartement(filiereEntrerDTO.getCodeDepartement());
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
        public void supprimerFiliere(String codeFiliere) throws StructureAcademiqueException {
            if (codeFiliere == null || codeFiliere.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la filière est obligatoire");
            }

            Filiere filiere = requireFiliere(codeFiliere);

            // Vérifier les dépendances
            List<Classe> classes = classeRepository.findByFiliere_CodeFiliere(codeFiliere);
            if (!classes.isEmpty()) {
                throw new StructureAcademiqueException("Suppression impossible : la filière contient des classes");
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

            if (classeEntrerDTO.getCodeFiliere() == null) {
                throw new StructureAcademiqueException("La classe doit être rattachée à une filière");
            }

            if (classeEntrerDTO.getEffectifMax() == null || classeEntrerDTO.getEffectifMax() <= 0) {
                throw new StructureAcademiqueException("L'effectif maximum est obligatoire et doit être supérieur à 0");
            }

            Classe classe = structureMapper.FromClasseEntrerVersClasse(classeEntrerDTO);

            // Récupérer la filière
            Filiere filiere = requireFiliere(classeEntrerDTO.getCodeFiliere());

            classe.setFiliere(filiere);

            Classe classeSauvegardee = classeRepository.save(classe);
            return structureMapper.FromClasseVersClasseSortieDTO(classeSauvegardee);
        }

        @Override
        public ClasseSortieDTO trouverClasseParCode(String codeClasse) throws StructureAcademiqueException {
            if (codeClasse == null || codeClasse.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la classe est obligatoire");
            }

            Classe classe = requireClasse(codeClasse);
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
        public List<ClasseSortieDTO> trouverClassesParFiliere(String codeFiliere) throws StructureAcademiqueException {
            if (codeFiliere == null || codeFiliere.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la filière est obligatoire");
            }

            Filiere filiere = requireFiliere(codeFiliere);

            try {
                List<Classe> classes = classeRepository.findByFiliere_CodeFiliere(codeFiliere);
                return structureMapper.FromListeClasseVersListeClasseSortieDTO(classes);
            } catch (Exception e) {
                throw new StructureAcademiqueException("Erreur lors de la recherche des classes par filière: " + e.getMessage(), e);
            }
        }

        @Override
        public ClasseSortieDTO modifierClasse(String codeClasse, ClasseEntrerDTO classeEntrerDTO) throws StructureAcademiqueException {
            if (codeClasse == null || codeClasse.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la classe est obligatoire");
            }

            if (classeEntrerDTO == null) {
                throw new StructureAcademiqueException("La classe ne peut pas être nulle");
            }

            Classe classe = requireClasse(codeClasse);

            // Valider l'unicité du code si modifié
            if (classeEntrerDTO.getCodeClasse() != null &&
                !classeEntrerDTO.getCodeClasse().equals(classe.getCodeClasse()) &&
                classeRepository.existsByCodeClasse(classeEntrerDTO.getCodeClasse())) {
                throw new StructureAcademiqueException("Une classe avec ce code existe déjà");
            }

            // Valider la filière si modifiée
            if (classeEntrerDTO.getCodeFiliere() != null && !classeEntrerDTO.getCodeFiliere().equals(classe.getFiliere().getCodeFiliere())) {
                Filiere filiere = requireFiliere(classeEntrerDTO.getCodeFiliere());
                classe.setFiliere(filiere);
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
            if (classeEntrerDTO.getEmploiTemps() != null) {
                classe.setEmploiTemps(classeEntrerDTO.getEmploiTemps());
            }

            Classe classeModifiee = classeRepository.save(classe);
            return structureMapper.FromClasseVersClasseSortieDTO(classeModifiee);
        }

        @Override
        public void supprimerClasse(String codeClasse) throws StructureAcademiqueException {
            if (codeClasse == null || codeClasse.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la classe est obligatoire");
            }

            Classe classe = requireClasse(codeClasse);

            // Vérifier les dépendances
            List<Etudiant> etudiants = etudiantRepository.findByClasse_CodeClasse(codeClasse);
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

            if (moduleEntrerDTO.getNom() == null || moduleEntrerDTO.getNom().trim().isEmpty()) {
                throw new StructureAcademiqueException("Le nom du module est obligatoire");
            }

            if (moduleEntrerDTO.getCodeFiliere() == null || moduleEntrerDTO.getCodeFiliere().trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la filière est obligatoire");
            }

            // Vérifier l'unicité du code
            if (moduleRepository.existsByCodeModule(moduleEntrerDTO.getCodeModule())) {
                throw new StructureAcademiqueException("Un module avec ce code existe déjà");
            }

            // Vérifier l'existence de la filière
            Filiere filiere = requireFiliere(moduleEntrerDTO.getCodeFiliere());

            Module module = structureMapper.FromModuleEntrerVersModule(moduleEntrerDTO);
            module.setFiliere(filiere);

            Module moduleSauvegarde = moduleRepository.save(module);
            return structureMapper.FromModuleVersModuleSortieDTO(moduleSauvegarde);
        }

        @Override
        public ModuleSortieDTO trouverModuleParCode(String codeModule) throws StructureAcademiqueException {
            if (codeModule == null || codeModule.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code du module est obligatoire");
            }

            Module module = requireModule(codeModule);
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
        public List<ModuleSortieDTO> trouverModulesParFiliere(String codeFiliere) throws StructureAcademiqueException {
            if (codeFiliere == null || codeFiliere.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la filière est obligatoire");
            }

            try {
                List<Module> modules = moduleRepository.findByFiliere_CodeFiliere(codeFiliere);
                return structureMapper.FromListeModuleVersListeModuleSortieDTO(modules);
            } catch (Exception e) {
                throw new StructureAcademiqueException("Erreur lors de la recherche des modules par filière: " + e.getMessage(), e);
            }
        }

        @Override
        public ModuleSortieDTO modifierModule(String codeModule, ModuleEntrerDTO moduleEntrerDTO) throws StructureAcademiqueException {
            if (codeModule == null || codeModule.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code du module est obligatoire");
            }

            if (moduleEntrerDTO == null) {
                throw new StructureAcademiqueException("Le module ne peut pas être nul");
            }

            Module module = requireModule(codeModule);

            // Valider l'unicité du code si modifié
            if (moduleEntrerDTO.getCodeModule() != null &&
                !moduleEntrerDTO.getCodeModule().equals(module.getCodeModule()) &&
                moduleRepository.existsByCodeModule(moduleEntrerDTO.getCodeModule())) {
                throw new StructureAcademiqueException("Un module avec ce code existe déjà");
            }

            // Valider la filière si modifiée
            if (moduleEntrerDTO.getCodeFiliere() != null && !moduleEntrerDTO.getCodeFiliere().equals(module.getFiliere().getCodeFiliere())) {
                Filiere filiere = requireFiliere(moduleEntrerDTO.getCodeFiliere());
                module.setFiliere(filiere);
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
        public void supprimerModule(String codeModule) throws StructureAcademiqueException {
            if (codeModule == null || codeModule.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code du module est obligatoire");
            }

            Module module = requireModule(codeModule);

            // Vérifier les dépendances
            List<Matiere> matieres = matiereRepository.findByModule_CodeModule(codeModule);
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

            if (matiereEntrerDTO.getNom() == null || matiereEntrerDTO.getNom().trim().isEmpty()) {
                throw new StructureAcademiqueException("Le nom de la matière est obligatoire");
            }

            if (matiereEntrerDTO.getCodeModule() == null || matiereEntrerDTO.getCodeModule().trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code du module est obligatoire");
            }

            // Vérifier l'unicité du code
            if (matiereRepository.existsByCodeMatiere(matiereEntrerDTO.getCodeMatiere())) {
                throw new StructureAcademiqueException("Une matière avec ce code existe déjà");
            }

            // Vérifier l'existence du module
            Module module = requireModule(matiereEntrerDTO.getCodeModule());

            Matiere matiere = structureMapper.FromMatiereEntrerVersMatiere(matiereEntrerDTO);
            matiere.setModule(module);

            Matiere matiereSauvegardee = matiereRepository.save(matiere);
            return structureMapper.FromMatiereVersMatiereSortieDTO(matiereSauvegardee);
        }

        @Override
        public MatiereSortieDTO trouverMatiereParCode(String codeMatiere) throws StructureAcademiqueException {
            if (codeMatiere == null || codeMatiere.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la matière est obligatoire");
            }

            Matiere matiere = requireMatiere(codeMatiere);
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
        public List<MatiereSortieDTO> trouverMatieresParModule(String codeModule) throws StructureAcademiqueException {
            if (codeModule == null || codeModule.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code du module est obligatoire");
            }

            try {
                List<Matiere> matieres = matiereRepository.findByModule_CodeModule(codeModule);
                return structureMapper.FromListeMatiereVersListeMatiereSortieDTO(matieres);
            } catch (Exception e) {
                throw new StructureAcademiqueException("Erreur lors de la recherche des matières par module: " + e.getMessage(), e);
            }
        }

        @Override
        public MatiereSortieDTO modifierMatiere(String codeMatiere, MatiereEntrerDTO matiereEntrerDTO) throws StructureAcademiqueException {
            if (codeMatiere == null || codeMatiere.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la matière est obligatoire");
            }

            if (matiereEntrerDTO == null) {
                throw new StructureAcademiqueException("La matière ne peut pas être nulle");
            }

            Matiere matiere = requireMatiere(codeMatiere);

            // Valider l'unicité du code si modifié
            if (matiereEntrerDTO.getCodeMatiere() != null &&
                !matiereEntrerDTO.getCodeMatiere().equals(matiere.getCodeMatiere()) &&
                matiereRepository.existsByCodeMatiere(matiereEntrerDTO.getCodeMatiere())) {
                throw new StructureAcademiqueException("Une matière avec ce code existe déjà");
            }

            // Valider le module si modifié
            if (matiereEntrerDTO.getCodeModule() != null && !matiereEntrerDTO.getCodeModule().equals(matiere.getModule().getCodeModule())) {
                Module module = requireModule(matiereEntrerDTO.getCodeModule());
                matiere.setModule(module);
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
        public void supprimerMatiere(String codeMatiere) throws StructureAcademiqueException {
            if (codeMatiere == null || codeMatiere.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la matière est obligatoire");
            }

            Matiere matiere = requireMatiere(codeMatiere);

            // Vérifier les dépendances
            List<AffectationEnseignement> affectations = affectationEnseignementRepository.findByMatiere_CodeMatiere(codeMatiere);
            if (!affectations.isEmpty()) {
                throw new StructureAcademiqueException("Suppression impossible : la matière est utilisée dans des affectations d'enseignement");
            }

            List<Examen> examens = examenRepository.findByMatiere_CodeMatiere(codeMatiere);
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
            if (affectationEnseignementEntrerDTO.getCodeClasse() == null) {
                throw new StructureAcademiqueException("Classe est obligatoire");
            }

            if (affectationEnseignementEntrerDTO.getCodeMatiere() == null) {
                throw new StructureAcademiqueException("Matiere est obligatoire");
            }

            AffectationEnseignement affectationEnseignement = structureMapper.FromAffectationEnseignementEntrerVersAffectationEnseignement(affectationEnseignementEntrerDTO);
            affectationEnseignement.setActive(true);

            affectationEnseignement.setProfesseur(requireProfesseur(affectationEnseignementEntrerDTO.getProfesseurPpr()));
            affectationEnseignement.setClasse(requireClasse(affectationEnseignementEntrerDTO.getCodeClasse()));
            affectationEnseignement.setMatiere(requireMatiere(affectationEnseignementEntrerDTO.getCodeMatiere()));

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

            // Charger l'affectation existante par ID
            AffectationEnseignement affectationExistante = affectationEnseignementRepository.findById(id).orElseThrow(() ->
                    new StructureAcademiqueException("Affectation introuvable avec l'identifiant: " + id));

            // Mise à jour des champs autorisés (null-safe)
            if (affectationEnseignementEntrerDTO.getAnneeUniversitaire() != null && !affectationEnseignementEntrerDTO.getAnneeUniversitaire().trim().isEmpty()) {
                affectationExistante.setAnneeUniversitaire(affectationEnseignementEntrerDTO.getAnneeUniversitaire());
            }

            if (affectationEnseignementEntrerDTO.getSemestre() != null) {
                affectationExistante.setSemestre(affectationEnseignementEntrerDTO.getSemestre());
            }

            // Mise à jour du professeur si fourni et différent
            if (affectationEnseignementEntrerDTO.getProfesseurPpr() != null &&
                (affectationExistante.getProfesseur() == null || !affectationEnseignementEntrerDTO.getProfesseurPpr().equals(affectationExistante.getProfesseur().getPpr()))) {

                Professeur professeur = requireProfesseur(affectationEnseignementEntrerDTO.getProfesseurPpr());
                affectationExistante.setProfesseur(professeur);
            }

            // Mise à jour de la classe si fournie et différente
            if (affectationEnseignementEntrerDTO.getCodeClasse() != null &&
                (affectationExistante.getClasse() == null || !affectationEnseignementEntrerDTO.getCodeClasse().equals(affectationExistante.getClasse().getCodeClasse()))) {

                Classe classe = requireClasse(affectationEnseignementEntrerDTO.getCodeClasse());
                affectationExistante.setClasse(classe);
            }

            // Mise à jour de la matière si fournie et différente
            if (affectationEnseignementEntrerDTO.getCodeMatiere() != null &&
                (affectationExistante.getMatiere() == null || !affectationEnseignementEntrerDTO.getCodeMatiere().equals(affectationExistante.getMatiere().getCodeMatiere()))) {

                Matiere matiere = requireMatiere(affectationEnseignementEntrerDTO.getCodeMatiere());
                affectationExistante.setMatiere(matiere);
            }

            // Mise à jour du statut actif si fourni
            if (affectationEnseignementEntrerDTO.getActive() != null) {
                affectationExistante.setActive(affectationEnseignementEntrerDTO.getActive());
            }

            AffectationEnseignement affectationModifiee = affectationEnseignementRepository.save(affectationExistante);
            return structureMapper.FromAffectationEnseignementVersAffectationEnseignementSortieDTO(affectationModifiee);
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
        public AffectationEnseignementSortieDTO trouverAffectationParId(Long id) throws StructureAcademiqueException {
            if (id == null) {
                throw new StructureAcademiqueException("L'identifiant de l'affectation est obligatoire");
            }

            AffectationEnseignement affectation = affectationEnseignementRepository.findById(id).orElseThrow(() ->
                    new StructureAcademiqueException("Affectation introuvable avec l'identifiant: " + id));

            return structureMapper.FromAffectationEnseignementVersAffectationEnseignementSortieDTO(affectation);
        }

        @Override
        public List<AffectationEnseignementSortieDTO> listerToutesLesAffectations() throws StructureAcademiqueException {
            try {
                List<AffectationEnseignement> affectations = affectationEnseignementRepository.findAll();
                return structureMapper.FromListeAffectationEnseignementVersListeAffectationEnseignementSortieDTO(affectations);
            } catch (Exception e) {
                throw new StructureAcademiqueException("Erreur lors de la liste des affectations: " + e.getMessage(), e);
            }
        }

        @Override
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
        public List<AffectationEnseignementSortieDTO> listerAffectationsParClasse(String codeClasse) throws StructureAcademiqueException {
            if (codeClasse == null || codeClasse.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la classe est obligatoire");
            }

            Classe classe = requireClasse(codeClasse);

            try {
                List<AffectationEnseignement> affectations = affectationEnseignementRepository.findByClasse_CodeClasse(codeClasse);
                return structureMapper.FromListeAffectationEnseignementVersListeAffectationEnseignementSortieDTO(affectations);
            } catch (Exception e) {
                throw new StructureAcademiqueException("Erreur lors de la recherche des affectations par classe: " + e.getMessage(), e);
            }
        }

        @Override
        public List<AffectationEnseignementSortieDTO> listerAffectationsParMatiere(String codeMatiere) throws StructureAcademiqueException {
            if (codeMatiere == null || codeMatiere.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la matière est obligatoire");
            }

            Matiere matiere = requireMatiere(codeMatiere);

            try {
                List<AffectationEnseignement> affectations = affectationEnseignementRepository.findByMatiere_CodeMatiere(codeMatiere);
                return structureMapper.FromListeAffectationEnseignementVersListeAffectationEnseignementSortieDTO(affectations);
            } catch (Exception e) {
                throw new StructureAcademiqueException("Erreur lors de la recherche des affectations par matière: " + e.getMessage(), e);
            }
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
        public void supprimerAffectationEnseignement(Long id) throws StructureAcademiqueException {
            if (id == null) {
                throw new StructureAcademiqueException("L'identifiant de l'affectation est obligatoire");
            }

            AffectationEnseignement affectation = affectationEnseignementRepository.findById(id).orElseThrow(() ->
                    new StructureAcademiqueException("Affectation introuvable avec l'identifiant: " + id));

            affectationEnseignementRepository.delete(affectation);
        }
    }
