    package com.mycampus.backend.metier.administrateur.structureAcademique;

    import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.DepartementEntrerDTO;
    import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.FiliereEntrerDTO;
    import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.ClasseEntrerDTO;
    import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.ModuleEntrerDTO;
    import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.MatiereEntrerDTO;
    import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.AffectationEnseignementEntrerDTO;
    import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.*;
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
        public DepartementSortieDTO modifierDepartement(String codeDepartement, DepartementModificationDTO departementModificationDTO) throws StructureAcademiqueException {
            if (codeDepartement == null || codeDepartement.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code du département est obligatoire");
            }

            if (departementModificationDTO == null) {
                throw new StructureAcademiqueException("Le département ne peut pas être nul");
            }

            Departement departement = requireDepartement(codeDepartement);

            // Gérer le chef de département si fourni
            if (departementModificationDTO.getChefDepartementPpr() != null && !departementModificationDTO.getChefDepartementPpr().trim().isEmpty()) {
                Professeur chefDepartement = requireProfesseur(departementModificationDTO.getChefDepartementPpr());
                departement.setChefDepartement(chefDepartement);
            }



            // Mettre à jour les champs autorisés

            if (departementModificationDTO.getTypeDepartement() != null && !departementModificationDTO.getTypeDepartement().trim().isEmpty()) {
                departement.setTypeDepartement(departementModificationDTO.getTypeDepartement());
            }
            if (departementModificationDTO.getDescription() != null) {
                departement.setDescription(departementModificationDTO.getDescription());
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
        public FiliereSortieDTO modifierFiliere(String codeFiliere, FiliereModificationDTO filiereModificationDTO) throws StructureAcademiqueException {
            if (codeFiliere == null || codeFiliere.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la filière est obligatoire");
            }

            if (filiereModificationDTO == null) {
                throw new StructureAcademiqueException("La filière ne peut pas être nulle");
            }

            Filiere filiere = requireFiliere(codeFiliere);

            // Gérer le chef de filière si fourni
            if (filiereModificationDTO.getChefFilierePpr() != null && !filiereModificationDTO.getChefFilierePpr().trim().isEmpty()) {
                Professeur chefFiliere = requireProfesseur(filiereModificationDTO.getChefFilierePpr());
                filiere.setChefFiliere(chefFiliere);
            }



            // Valider le département si modifié
            if (filiereModificationDTO.getCodeDepartement() != null && !filiereModificationDTO.getCodeDepartement().equals(filiere.getDepartement().getCodeDepartement())) {
                Departement departement = requireDepartement(filiereModificationDTO.getCodeDepartement());
                filiere.setDepartement(departement);
            }

            // Mettre à jour les champs autorisés

            if (filiereModificationDTO.getNom() != null && !filiereModificationDTO.getNom().trim().isEmpty()) {
                filiere.setNom(filiereModificationDTO.getNom());
            }
            if (filiereModificationDTO.getDescription() != null) {
                filiere.setDescription(filiereModificationDTO.getDescription());
            }

            if (filiereModificationDTO.getDureeFormation() != null && !filiereModificationDTO.getDureeFormation().trim().isEmpty()) {
                filiere.setDureeFormation(filiereModificationDTO.getDureeFormation());
            }
            if (filiereModificationDTO.getNiveauDiplome() != null && !filiereModificationDTO.getNiveauDiplome().trim().isEmpty()) {
                filiere.setNiveauDiplome(filiereModificationDTO.getNiveauDiplome());
            }
            if (filiereModificationDTO.getEtat() != null) {
                filiere.setEtat(filiereModificationDTO.getEtat());
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
        public ClasseSortieDTO modifierClasse(String codeClasse, ClasseModificationDTO classeModificationDTO) throws StructureAcademiqueException {
            if (codeClasse == null || codeClasse.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la classe est obligatoire");
            }

            if (classeModificationDTO == null) {
                throw new StructureAcademiqueException("La classe ne peut pas être nulle");
            }

            Classe classe = requireClasse(codeClasse);



            // Valider la filière si modifiée
            if (classeModificationDTO.getCodeFiliere() != null && !classeModificationDTO.getCodeFiliere().equals(classe.getFiliere().getCodeFiliere())) {
                Filiere filiere = requireFiliere(classeModificationDTO.getCodeFiliere());
                classe.setFiliere(filiere);
            }

            // Mettre à jour les champs autorisés

            if (classeModificationDTO.getNom() != null && !classeModificationDTO.getNom().trim().isEmpty()) {
                classe.setNom(classeModificationDTO.getNom());
            }
            if (classeModificationDTO.getNiveau() != null && !classeModificationDTO.getNiveau().trim().isEmpty()) {
                classe.setNiveau(classeModificationDTO.getNiveau());
            }
            if (classeModificationDTO.getAnneeUniversitaire() != null && !classeModificationDTO.getAnneeUniversitaire().trim().isEmpty()) {
                classe.setAnneeUniversitaire(classeModificationDTO.getAnneeUniversitaire());
            }
            if (classeModificationDTO.getEffectifMax() != null) {
                classe.setEffectifMax(classeModificationDTO.getEffectifMax());
            }
            if (classeModificationDTO.getEmploiTemps() != null) {
                classe.setEmploiTemps(classeModificationDTO.getEmploiTemps());
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
        public ModuleSortieDTO modifierModule(String codeModule, ModuleModificationDTO moduleModificationDTO) throws StructureAcademiqueException {
            if (codeModule == null || codeModule.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code du module est obligatoire");
            }

            if (moduleModificationDTO == null) {
                throw new StructureAcademiqueException("Le module ne peut pas être nul");
            }

            Module module = requireModule(codeModule);



            // Valider la filière si modifiée
            if (moduleModificationDTO.getCodeFiliere() != null && !moduleModificationDTO.getCodeFiliere().equals(module.getFiliere().getCodeFiliere())) {
                Filiere filiere = requireFiliere(moduleModificationDTO.getCodeFiliere());
                module.setFiliere(filiere);
            }


            if (moduleModificationDTO.getNom() != null && !moduleModificationDTO.getNom().trim().isEmpty()) {
                module.setNom(moduleModificationDTO.getNom());
            }
            if (moduleModificationDTO.getDescription() != null) {
                module.setDescription(moduleModificationDTO.getDescription());
            }
            if (moduleModificationDTO.getSemestre() != null) {
                module.setSemestre(moduleModificationDTO.getSemestre());
            }
            if (moduleModificationDTO.getCoefficient() != null) {
                module.setCoefficient(moduleModificationDTO.getCoefficient());
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
        public MatiereSortieDTO modifierMatiere(String codeMatiere, MatiereModificationDTO matiereModificationDTO) throws StructureAcademiqueException {
            if (codeMatiere == null || codeMatiere.trim().isEmpty()) {
                throw new StructureAcademiqueException("Le code de la matière est obligatoire");
            }

            if (matiereModificationDTO == null) {
                throw new StructureAcademiqueException("La matière ne peut pas être nulle");
            }

            Matiere matiere = requireMatiere(codeMatiere);

            // Valider l'unicité du code si modifié


            // Valider le module si modifié
            if (matiereModificationDTO.getCodeModule() != null && !matiereModificationDTO.getCodeModule().equals(matiere.getModule().getCodeModule())) {
                Module module = requireModule(matiereModificationDTO.getCodeModule());
                matiere.setModule(module);
            }

            // Mettre à jour les champs autorisés

            if (matiereModificationDTO.getNom() != null && !matiereModificationDTO.getNom().trim().isEmpty()) {
                matiere.setNom(matiereModificationDTO.getNom());
            }
            if (matiereModificationDTO.getDescription() != null) {
                matiere.setDescription(matiereModificationDTO.getDescription());
            }
            if (matiereModificationDTO.getCoefficient() != null) {
                matiere.setCoefficient(matiereModificationDTO.getCoefficient());
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
        public AffectationEnseignementSortieDTO modifierAffectationEnseignement(Long id, AffectationEnseignementModificationDTO affectationEnseignementModificationDTO) throws StructureAcademiqueException {
            if (id == null) {
                throw new StructureAcademiqueException("L'identifiant de l'affectation est obligatoire");
            }

            if (affectationEnseignementModificationDTO == null) {
                throw new StructureAcademiqueException("L'affectation ne peut pas être nulle");
            }

            // Charger l'affectation existante par ID
            AffectationEnseignement affectationExistante = affectationEnseignementRepository.findById(id).orElseThrow(() ->
                    new StructureAcademiqueException("Affectation introuvable avec l'identifiant: " + id));

            // Mise à jour des champs autorisés (null-safe)
            if (affectationEnseignementModificationDTO.getAnneeUniversitaire() != null && !affectationEnseignementModificationDTO.getAnneeUniversitaire().trim().isEmpty()) {
                affectationExistante.setAnneeUniversitaire(affectationEnseignementModificationDTO.getAnneeUniversitaire());
            }

            if (affectationEnseignementModificationDTO.getSemestre() != null) {
                affectationExistante.setSemestre(affectationEnseignementModificationDTO.getSemestre());
            }

            // Mise à jour du professeur si fourni et différent
            if (affectationEnseignementModificationDTO.getProfesseurPpr() != null &&
                (affectationExistante.getProfesseur() == null || !affectationEnseignementModificationDTO.getProfesseurPpr().equals(affectationExistante.getProfesseur().getPpr()))) {

                Professeur professeur = requireProfesseur(affectationEnseignementModificationDTO.getProfesseurPpr());
                affectationExistante.setProfesseur(professeur);
            }

            // Mise à jour de la classe si fournie et différente
            if (affectationEnseignementModificationDTO.getCodeClasse() != null &&
                (affectationExistante.getClasse() == null || !affectationEnseignementModificationDTO.getCodeClasse().equals(affectationExistante.getClasse().getCodeClasse()))) {

                Classe classe = requireClasse(affectationEnseignementModificationDTO.getCodeClasse());
                affectationExistante.setClasse(classe);
            }

            // Mise à jour de la matière si fournie et différente
            if (affectationEnseignementModificationDTO.getCodeMatiere() != null &&
                (affectationExistante.getMatiere() == null || !affectationEnseignementModificationDTO.getCodeMatiere().equals(affectationExistante.getMatiere().getCodeMatiere()))) {

                Matiere matiere = requireMatiere(affectationEnseignementModificationDTO.getCodeMatiere());
                affectationExistante.setMatiere(matiere);
            }

            // Mise à jour du statut actif si fourni
            if (affectationEnseignementModificationDTO.getActive() != null) {
                affectationExistante.setActive(affectationEnseignementModificationDTO.getActive());
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
