package com.mycampus.backend;

import com.mycampus.backend.dto.administrateur.DonneEntrer.DepartementEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.FiliereEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.ClasseEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.ModuleEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.MatiereEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.AffectationEnseignementEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.AdministrateurEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.ProfesseurEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.EtudiantEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.DepartementSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.FiliereSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ClasseSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ModuleSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.MatiereSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.AffectationEnseignementSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.AdministrateurSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ProfesseurSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.EtudiantSortieDTO;
import com.mycampus.backend.enumerations.*;

import com.mycampus.backend.exceptions.StructureAcademiqueException;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.metier.administrateur.structureAcademique.GestionStructureAcademiqueMetier;
import com.mycampus.backend.metier.administrateur.utilisateurs.GestionUtilisateursMetier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class BackendApplication {

    private static final Logger logger = LoggerFactory.getLogger(BackendApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    CommandLineRunner testMetierLayers(
        GestionStructureAcademiqueMetier gestionStructureAcademiqueMetier,
        GestionUtilisateursMetier gestionUtilisateursMetier
    ) {
        return args -> {
            logger.info("=== DÉMARRAGE DES TESTS DES MÉTIERS ===");
            
            // Phase 1: Structure Académique (prérequis pour les utilisateurs)
            testCreateStructureAcademique(gestionStructureAcademiqueMetier, gestionUtilisateursMetier);
            testOtherStructureAcademique(gestionStructureAcademiqueMetier);
            testUpdateStructureAcademique(gestionStructureAcademiqueMetier);
            testOtherStructureAcademique(gestionStructureAcademiqueMetier);
            //testDeleteStructureAcademique(gestionStructureAcademiqueMetier);
            
            // Phase 2: Utilisateurs (dépend de la structure)
            testCreateUtilisateurs(gestionUtilisateursMetier);
            testOtherUtilisateurs(gestionUtilisateursMetier);
            testUpdateUtilisateurs(gestionUtilisateursMetier);
            testOtherUtilisateurs(gestionUtilisateursMetier);
            //testDeleteUtilisateurs(gestionUtilisateursMetier);
            
            logger.info("=== FIN DES TESTS DES MÉTIERS ===");
        };
    }

    // Structure Académique Tests
    private void testCreateStructureAcademique(GestionStructureAcademiqueMetier metier, GestionUtilisateursMetier utilisateursMetier) {
        logger.info("[STRUCTURE] === CRÉATION DES ENTITÉS ===");
        
        try {
            // Créer département
            DepartementEntrerDTO depIn = DepartementEntrerDTO.builder()
                .codeDepartement("INFO")
                .typeDepartement("Informatique")
                .description("Département d'informatique")
                .build();
            DepartementSortieDTO depOut = metier.creerDepartement(depIn);
            logger.info("[STRUCTURE] Création département OK: id={}, code={}", depOut.getId(), depOut.getCodeDepartement());

            // Créer filière
            FiliereEntrerDTO filIn = FiliereEntrerDTO.builder()
                .codeFiliere("ILCS")
                .nom("Informatique Licence Cyber Sécurité")
                .description("Formation en cybersécurité")
                .dureeFormation("3 Ans")
                .niveauDiplome("Licence")
                .etat(EtatStructure.OUVERTE)
                .departementId(depOut.getId())
                .build();
            FiliereSortieDTO filOut = metier.creerFiliere(filIn);
            logger.info("[STRUCTURE] Création filière OK: id={}, nom={}", filOut.getId(), filOut.getNom());

            // Créer classe
            ClasseEntrerDTO clsIn = ClasseEntrerDTO.builder()
                .codeClasse("ILCS1")
                .nom("ILCS Semestre 1")
                .niveau("1ère année")
                .anneeUniversitaire("2025/2026")
                .effectifMax(30)
                .filiereId(filOut.getId())
                .build();
            ClasseSortieDTO clsOut = metier.creerClasse(clsIn);
            logger.info("[STRUCTURE] Création classe OK: id={}, nom={}", clsOut.getId(), clsOut.getNom());
            // Créer étudiant (après création de la classe)
            EtudiantEntrerDTO etudIn = EtudiantEntrerDTO.builder()
                    .apogee("202300123")
                    .nom("Bernard")
                    .prenom("Sophie")
                    .email("sophie.bernard@mycampus.ma")
                    .cin("CC123789")
                    .password("password789")
                    .tel("0634567890")
                    .niveau("1ère année")
                    .anneeInscription(2025)
                    .semestreActuel(Semestre.S1)
                    .classeId(clsOut.getId())
                    .dateNaissance(LocalDate.of(2002, 5, 15))
                    .etatInscription(EtatInscription.INSCRIT)
                    .build();
            EtudiantSortieDTO etudOut = utilisateursMetier.creerEtudiant(etudIn);
            logger.info("[UTILISATEURS] Création étudiant OK: id={}, prenom={}, nom={}, apogee={}, classeId={}", etudOut.getId(), etudOut.getPrenom(), etudOut.getNom(), etudOut.getApogee(), etudOut.getClasseId());
            // Créer professeur
            ProfesseurEntrerDTO profIn = ProfesseurEntrerDTO.builder()
                    .ppr("87654321")
                    .nom("Martin")
                    .prenom("Pierre")
                    .email("pierre.martin@mycampus.ma")
                    .cin("BB987654")
                    .password("password456")
                    .tel("0623456789")
                    .specialite("Informatique")
                    .grade("PES")
                    .statut("Titulaire")
                    .dateRecrutement(LocalDate.of(2020, 9, 1))
                    .build();
            ProfesseurSortieDTO profOut = utilisateursMetier.creerProfesseur(profIn);
            logger.info("[UTILISATEURS] Création professeur OK: id={}, prenom={}, nom={}, specialite={}", profOut.getId(), profOut.getPrenom(), profOut.getNom(), profOut.getSpecialite());

            // Créer module
            ModuleEntrerDTO modIn = ModuleEntrerDTO.builder()
                .codeModule("MATH101")
                .nom("Mathématiques Fondamentales")
                .description("Module de mathématiques de base")
                .semestre(Semestre.S1)
                .coefficient(3)
                .filiereId(filOut.getId())
                .build();
            ModuleSortieDTO modOut = metier.creerModule(modIn);
            logger.info("[STRUCTURE] Création module OK: id={}, nom={}", modOut.getId(), modOut.getNom());
            
            // Créer matière
            MatiereEntrerDTO matIn = MatiereEntrerDTO.builder()
                .codeMatiere("ALGEBRE")
                .nom("Algèbre Linéaire")
                .description("Cours d'algèbre linéaire")
                .coefficient(2)
                .moduleId(modOut.getId())
                .build();
            MatiereSortieDTO matOut = metier.creerMatiere(matIn);
            logger.info("[STRUCTURE] Création matière OK: id={}, nom={}", matOut.getId(), matOut.getNom());
            
            // Créer affectation enseignement
            AffectationEnseignementEntrerDTO affIn = AffectationEnseignementEntrerDTO.builder()
                .anneeUniversitaire("2025/2026")
                .semestre(Semestre.S1)
                .matiereId(matOut.getId())
                .professeurPpr("87654321")
                .classeId(clsOut.getId())
                .build();
            AffectationEnseignementSortieDTO affOut = metier.creerAffectationEnseignement(affIn);
            logger.info("[STRUCTURE] Création affectation OK: id={}, annee={}", affOut.getId(), affOut.getAnneeUniversitaire());
            
        } catch (StructureAcademiqueException e) {
            logger.error("[STRUCTURE] ERREUR dans testCreateStructureAcademique: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[STRUCTURE] ERREUR INATTENDUE dans testCreateStructureAcademique: {}", e.getMessage(), e);
        }
    }
    
    private void testUpdateStructureAcademique(GestionStructureAcademiqueMetier metier) {
        logger.info("[STRUCTURE] === MISE À JOUR DES ENTITÉS ===");
        
        try {
            // Obtenir les IDs depuis les listes existantes
            List<DepartementSortieDTO> departements = metier.listerTousLesDepartements();
            List<FiliereSortieDTO> filieres = metier.listerToutesLesFilieres();
            List<ClasseSortieDTO> classes = metier.listerToutesLesClasses();
            List<ModuleSortieDTO> modules = metier.listerTousLesModules();
            List<MatiereSortieDTO> matieres = metier.listerToutesLesMatieres();
            List<AffectationEnseignementSortieDTO> affectations = metier.listerToutesLesAffectations();
            
            if (!departements.isEmpty()) {
                Long depId = departements.get(0).getId();
                DepartementEntrerDTO depUp = DepartementEntrerDTO.builder()
                    .codeDepartement("INFO")
                    .typeDepartement("Informatique et Réseaux")
                    .description("Département d'informatique et réseaux")
                    .build();
                DepartementSortieDTO depMod = metier.modifierDepartement(depId, depUp);
                logger.info("[STRUCTURE] Mise à jour département OK: id={}, description={}", depMod.getId(), depMod.getDescription());
            }
            
            if (!filieres.isEmpty()) {
                Long filId = filieres.get(0).getId();
                FiliereEntrerDTO filUp = FiliereEntrerDTO.builder()
                    .codeFiliere("ILCS")
                    .nom("Informatique Licence Cybersécurité Spécialisée")
                    .description("Formation spécialisée en cybersécurité")
                    .dureeFormation("3 Ans")
                    .niveauDiplome("Licence")
                    .etat(EtatStructure.OUVERTE)
                    .departementId(departements.get(0).getId())
                    .build();
                FiliereSortieDTO filMod = metier.modifierFiliere(filId, filUp);
                logger.info("[STRUCTURE] Mise à jour filière OK: id={}, nom={}", filMod.getId(), filMod.getNom());
            }
            
            if (!classes.isEmpty()) {
                Long clsId = classes.get(0).getId();
                ClasseEntrerDTO clsUp = ClasseEntrerDTO.builder()
                    .codeClasse("ILCS1")
                    .nom("ILCS Semestre 1 - Groupe A")
                    .niveau("1ère année")
                    .anneeUniversitaire("2025/2026")
                    .effectifMax(35)
                    .filiereId(filieres.get(0).getId())
                    .build();
                ClasseSortieDTO clsMod = metier.modifierClasse(clsId, clsUp);
                logger.info("[STRUCTURE] Mise à jour classe OK: id={}, nom={}, effectif={}", clsMod.getId(), clsMod.getNom(), clsMod.getEffectifMax());
            }
            
            if (!modules.isEmpty()) {
                Long modId = modules.get(0).getId();
                ModuleEntrerDTO modUp = ModuleEntrerDTO.builder()
                    .codeModule("MATH101")
                    .nom("Mathématiques Appliquées")
                    .description("Module de mathématiques appliquées")
                    .semestre(Semestre.S1)
                    .coefficient(4)
                    .filiereId(filieres.get(0).getId())
                    .build();
                ModuleSortieDTO modMod = metier.modifierModule(modId, modUp);
                logger.info("[STRUCTURE] Mise à jour module OK: id={}, nom={}, coefficient={}", modMod.getId(), modMod.getNom(), modMod.getCoefficient());
            }
            
            if (!matieres.isEmpty()) {
                Long matId = matieres.get(0).getId();
                MatiereEntrerDTO matUp = MatiereEntrerDTO.builder()
                    .codeMatiere("ALGEBRE")
                    .nom("Algèbre Linéaire Avancée")
                    .description("Cours avancé d'algèbre linéaire")
                    .coefficient(3)
                    .moduleId(modules.get(0).getId())
                    .build();
                MatiereSortieDTO matMod = metier.modifierMatiere(matId, matUp);
                logger.info("[STRUCTURE] Mise à jour matière OK: id={}, nom={}, coefficient={}", matMod.getId(), matMod.getNom(), matMod.getCoefficient());
            }
            
            if (!affectations.isEmpty()) {
                Long affId = affectations.get(0).getId();
                AffectationEnseignementEntrerDTO affUp = AffectationEnseignementEntrerDTO.builder()
                    .anneeUniversitaire("2025/2026")
                    .semestre(Semestre.S2)
                    .matiereId(matieres.get(0).getId())
                    .professeurPpr("87654321")
                    .classeId(classes.get(0).getId())
                    .active(true)
                    .build();
                AffectationEnseignementSortieDTO affMod = metier.modifierAffectationEnseignement(affId, affUp);
                logger.info("[STRUCTURE] Mise à jour affectation OK: id={}, semestre={}", affMod.getId(), affMod.getSemestre());
            }
            
        } catch (StructureAcademiqueException e) {
            logger.error("[STRUCTURE] ERREUR dans testUpdateStructureAcademique: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[STRUCTURE] ERREUR INATTENDUE dans testUpdateStructureAcademique: {}", e.getMessage(), e);
        }
    }
    
    private void testDeleteStructureAcademique(GestionStructureAcademiqueMetier metier) {
        logger.info("[STRUCTURE] === SUPPRESSION DES ENTITÉS ===");
        
        try {
            // Obtenir les IDs depuis les listes existantes
            List<AffectationEnseignementSortieDTO> affectations = metier.listerToutesLesAffectations();
            List<MatiereSortieDTO> matieres = metier.listerToutesLesMatieres();
            List<ModuleSortieDTO> modules = metier.listerTousLesModules();
            List<ClasseSortieDTO> classes = metier.listerToutesLesClasses();
            List<FiliereSortieDTO> filieres = metier.listerToutesLesFilieres();
            List<DepartementSortieDTO> departements = metier.listerTousLesDepartements();
            
            // Supprimer dans l'ordre inverse pour respecter les dépendances
            for (AffectationEnseignementSortieDTO aff : affectations) {
                metier.supprimerAffectationEnseignement(aff.getId());
                logger.info("[STRUCTURE] Suppression affectation confirmée: id={}", aff.getId());
            }
            
            for (MatiereSortieDTO mat : matieres) {
                metier.supprimerMatiere(mat.getId());
                logger.info("[STRUCTURE] Suppression matière confirmée: id={}", mat.getId());
            }
            
            for (ModuleSortieDTO mod : modules) {
                metier.supprimerModule(mod.getId());
                logger.info("[STRUCTURE] Suppression module confirmée: id={}", mod.getId());
            }
            
            for (ClasseSortieDTO cls : classes) {
                metier.supprimerClasse(cls.getId());
                logger.info("[STRUCTURE] Suppression classe confirmée: id={}", cls.getId());
            }
            
            for (FiliereSortieDTO fil : filieres) {
                metier.supprimerFiliere(fil.getId());
                logger.info("[STRUCTURE] Suppression filière confirmée: id={}", fil.getId());
            }
            
            for (DepartementSortieDTO dep : departements) {
                metier.supprimerDepartement(dep.getId());
                logger.info("[STRUCTURE] Suppression département confirmée: id={}", dep.getId());
            }
            
        } catch (StructureAcademiqueException e) {
            logger.error("[STRUCTURE] ERREUR dans testDeleteStructureAcademique: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[STRUCTURE] ERREUR INATTENDUE dans testDeleteStructureAcademique: {}", e.getMessage(), e);
        }
    }
    
    private void testOtherStructureAcademique(GestionStructureAcademiqueMetier metier) {
        logger.info("[STRUCTURE] === OPÉRATIONS DE LECTURE ET VÉRIFICATION ===");
        
        try {
            // Lister toutes les entités
            List<DepartementSortieDTO> departements = metier.listerTousLesDepartements();
            logger.info("[STRUCTURE] Total départements: {}", departements.size());
            
            List<FiliereSortieDTO> filieres = metier.listerToutesLesFilieres();
            logger.info("[STRUCTURE] Total filières: {}", filieres.size());
            
            List<ClasseSortieDTO> classes = metier.listerToutesLesClasses();
            logger.info("[STRUCTURE] Total classes: {}", classes.size());
            
            List<ModuleSortieDTO> modules = metier.listerTousLesModules();
            logger.info("[STRUCTURE] Total modules: {}", modules.size());
            
            List<MatiereSortieDTO> matieres = metier.listerToutesLesMatieres();
            logger.info("[STRUCTURE] Total matières: {}", matieres.size());
            
            List<AffectationEnseignementSortieDTO> affectations = metier.listerToutesLesAffectations();
            logger.info("[STRUCTURE] Total affectations: {}", affectations.size());
            
            // Tests de recherche par relations
            if (!departements.isEmpty()) {
                List<FiliereSortieDTO> filieresParDep = metier.trouverFilieresParDepartement(departements.get(0).getId());
                logger.info("[STRUCTURE] Filieres par département {}: {}", departements.get(0).getId(), filieresParDep.size());
            }
            
            if (!filieres.isEmpty()) {
                List<ClasseSortieDTO> classesParFil = metier.trouverClassesParFiliere(filieres.get(0).getId());
                logger.info("[STRUCTURE] Classes par filière {}: {}", filieres.get(0).getId(), classesParFil.size());
                
                List<ModuleSortieDTO> modulesParFil = metier.trouverModulesParFiliere(filieres.get(0).getId());
                logger.info("[STRUCTURE] Modules par filière {}: {}", filieres.get(0).getId(), modulesParFil.size());
            }
            
            if (!modules.isEmpty()) {
                List<MatiereSortieDTO> matieresParMod = metier.trouverMatieresParModule(modules.get(0).getId());
                logger.info("[STRUCTURE] Matières par module {}: {}", modules.get(0).getId(), matieresParMod.size());
            }
            
            if (!affectations.isEmpty()) {
                List<AffectationEnseignementSortieDTO> affParProf = metier.listerAffectationsParProfesseur(affectations.get(0).getProfesseurPpr());
                logger.info("[STRUCTURE] Affectations par professeur {}: {}", affectations.get(0).getProfesseurPpr(), affParProf.size());
                
                List<AffectationEnseignementSortieDTO> affParClasse = metier.listerAffectationsParClasse(affectations.get(0).getClasseId());
                logger.info("[STRUCTURE] Affectations par classe {}: {}", affectations.get(0).getClasseId(), affParClasse.size());
                
                List<AffectationEnseignementSortieDTO> affParMatiere = metier.listerAffectationsParMatiere(affectations.get(0).getMatiereId());
                logger.info("[STRUCTURE] Affectations par matière {}: {}", affectations.get(0).getMatiereId(), affParMatiere.size());
            }
            
            // Tests d'activation/désactivation
            if (!affectations.isEmpty()) {
                Long affId = affectations.get(0).getId();
                metier.desactiverAffectationEnseignement(affId);
                logger.info("[STRUCTURE] Affectation désactivée: id={}", affId);
                
                metier.activerAffectationEnseignement(affId);
                logger.info("[STRUCTURE] Affectation réactivée: id={}", affId);
            }
            
        } catch (StructureAcademiqueException e) {
            logger.error("[STRUCTURE] ERREUR dans testOtherStructureAcademique: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[STRUCTURE] ERREUR INATTENDUE dans testOtherStructureAcademique: {}", e.getMessage(), e);
        }
    }

    // Utilisateurs Tests
    private void testCreateUtilisateurs(GestionUtilisateursMetier metier) {
        logger.info("[UTILISATEURS] === CRÉATION DES UTILISATEURS ===");
        
        try {
            // Créer administrateur
            AdministrateurEntrerDTO adminIn = AdministrateurEntrerDTO.builder()
                .ppr("12345678")
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@mycampus.ma")
                .cin("AA123456")
                .password("password123")
                .tel("0612345678")
                .fonction("Administrateur Système")
                .grade("A1")
                .build();
            AdministrateurSortieDTO adminOut = metier.creerAdministrateur(adminIn);
            logger.info("[UTILISATEURS] Création administrateur OK: id={}, prenom={}, nom={}", adminOut.getId(), adminOut.getPrenom(), adminOut.getNom());
            

           
        } catch (UtilisateursException e) {
            logger.error("[UTILISATEURS] ERREUR dans testCreateUtilisateurs: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[UTILISATEURS] ERREUR INATTENDUE dans testCreateUtilisateurs: {}", e.getMessage(), e);
        }
    }
    
    private void testUpdateUtilisateurs(GestionUtilisateursMetier metier) {
        logger.info("[UTILISATEURS] === MISE À JOUR DES UTILISATEURS ===");
        
        try {
            // Obtenir les IDs depuis les listes existantes
            List<AdministrateurSortieDTO> admins = metier.listerTousLesAdministrateurs();
            List<ProfesseurSortieDTO> profs = metier.listerTousLesProfesseurs();
            List<EtudiantSortieDTO> etuds = metier.listerTousLesEtudiants();
            
            if (!admins.isEmpty()) {
                Long adminId = admins.get(0).getId();
                AdministrateurEntrerDTO adminUp = AdministrateurEntrerDTO.builder()
                    .ppr("12345678")
                    .nom("Dupont")
                    .prenom("Jean")
                    .email("jean.dupont.modifie@mycampus.ma")
                    .cin("AA123456")
                    .tel("0698765432")
                    .fonction("Administrateur Principal")
                    .grade("A2")
                    .build();
                AdministrateurSortieDTO adminMod = metier.modifierAdministrateur(adminId, adminUp);
                logger.info("[UTILISATEURS] Mise à jour administrateur OK: id={}, fonction={}, grade={}", adminMod.getId(), adminMod.getFonction(), adminMod.getGrade());
            }
            
            if (!profs.isEmpty()) {
                Long profId = profs.get(0).getId();
                ProfesseurEntrerDTO profUp = ProfesseurEntrerDTO.builder()
                    .ppr("87654321")
                    .nom("Martin")
                    .prenom("Pierre")
                    .email("pierre.martin.modifie@mycampus.ma")
                    .cin("BB987654")
                    .tel("0623456789")
                    .specialite("Intelligence Artificielle")
                    .grade("PH")
                    .statut("Titulaire")
                    .dateRecrutement(LocalDate.of(2020, 9, 1))
                    .build();
                ProfesseurSortieDTO profMod = metier.modifierProfesseur(profId, profUp);
                logger.info("[UTILISATEURS] Mise à jour professeur OK: id={}, specialite={}, grade={}", profMod.getId(), profMod.getSpecialite(), profMod.getGrade());
            }
            
            if (!etuds.isEmpty()) {
                Long etudId = etuds.get(0).getId();
                EtudiantEntrerDTO etudUp = EtudiantEntrerDTO.builder()
                    .apogee("202300123")
                    .nom("Bernard")
                    .prenom("Sophie")
                    .email("sophie.bernard.modifie@mycampus.ma")
                    .cin("CC123789")
                    .tel("0634567890")
                    .niveau("4ème année")
                    .anneeInscription(2023)
                    .semestreActuel(Semestre.S2)
                    .dateNaissance(LocalDate.of(2002, 5, 15))
                    .etatInscription(EtatInscription.INSCRIT)
                    .build();
                EtudiantSortieDTO etudMod = metier.modifierEtudiant(etudId, etudUp);
                logger.info("[UTILISATEURS] Mise à jour étudiant OK: id={}, niveau={}, semestre={}", etudMod.getId(), etudMod.getNiveau(), etudMod.getSemestreActuel());
            }
            
        } catch (UtilisateursException e) {
            logger.error("[UTILISATEURS] ERREUR dans testUpdateUtilisateurs: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[UTILISATEURS] ERREUR INATTENDUE dans testUpdateUtilisateurs: {}", e.getMessage(), e);
        }
    }
    
    private void testDeleteUtilisateurs(GestionUtilisateursMetier metier) {
        logger.info("[UTILISATEURS] === SUPPRESSION DES UTILISATEURS ===");
        
        try {
            // Obtenir les IDs depuis les listes existantes
            List<EtudiantSortieDTO> etuds = metier.listerTousLesEtudiants();
            List<ProfesseurSortieDTO> profs = metier.listerTousLesProfesseurs();
            List<AdministrateurSortieDTO> admins = metier.listerTousLesAdministrateurs();
            
            // Supprimer dans l'ordre inverse pour respecter les dépendances
            for (EtudiantSortieDTO etud : etuds) {
                metier.supprimerEtudiant(etud.getId());
                logger.info("[UTILISATEURS] Suppression étudiant confirmée: id={}", etud.getId());
            }
            
            for (ProfesseurSortieDTO prof : profs) {
                metier.supprimerProfesseur(prof.getId());
                logger.info("[UTILISATEURS] Suppression professeur confirmée: id={}", prof.getId());
            }
            
            for (AdministrateurSortieDTO admin : admins) {
                metier.supprimerAdministrateur(admin.getId());
                logger.info("[UTILISATEURS] Suppression administrateur confirmée: id={}", admin.getId());
            }
            
        } catch (UtilisateursException e) {
            logger.error("[UTILISATEURS] ERREUR dans testDeleteUtilisateurs: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[UTILISATEURS] ERREUR INATTENDUE dans testDeleteUtilisateurs: {}", e.getMessage(), e);
        }
    }
    
    private void testOtherUtilisateurs(GestionUtilisateursMetier metier) {
        logger.info("[UTILISATEURS] === OPÉRATIONS DE LECTURE ET VÉRIFICATION ===");
        
        try {
            // Lister tous les utilisateurs
            List<AdministrateurSortieDTO> admins = metier.listerTousLesAdministrateurs();
            logger.info("[UTILISATEURS] Total administrateurs: {}", admins.size());
            
            List<ProfesseurSortieDTO> profs = metier.listerTousLesProfesseurs();
            logger.info("[UTILISATEURS] Total professeurs: {}", profs.size());
            
            List<EtudiantSortieDTO> etuds = metier.listerTousLesEtudiants();
            logger.info("[UTILISATEURS] Total étudiants: {}", etuds.size());
            
            // Tests de recherche par identifiants
            if (!admins.isEmpty()) {
                AdministrateurSortieDTO adminParPpr = metier.trouverAdministrateurParPpr(admins.get(0).getPpr());
                logger.info("[UTILISATEURS] Administrateur par PPR {}: email={}", adminParPpr.getPpr(), adminParPpr.getEmail());
                
                AdministrateurSortieDTO adminParId = metier.trouverAdministrateurParId(admins.get(0).getId());
                logger.info("[UTILISATEURS] Administrateur par ID {}: fonction={}", adminParId.getId(), adminParId.getFonction());
            }
            
            if (!profs.isEmpty()) {
                ProfesseurSortieDTO profParPpr = metier.trouverProfesseurParPpr(profs.get(0).getPpr());
                logger.info("[UTILISATEURS] Professeur par PPR {}: specialite={}", profParPpr.getPpr(), profParPpr.getSpecialite());
                
                ProfesseurSortieDTO profParId = metier.trouverProfesseurParId(profs.get(0).getId());
                logger.info("[UTILISATEURS] Professeur par ID {}: grade={}", profParId.getId(), profParId.getGrade());
            }
            
            if (!etuds.isEmpty()) {
                EtudiantSortieDTO etudParApogee = metier.trouverEtudiantParApogee(etuds.get(0).getApogee());
                logger.info("[UTILISATEURS] Étudiant par Apogée {}: niveau={}", etudParApogee.getApogee(), etudParApogee.getNiveau());
                
                EtudiantSortieDTO etudParId = metier.trouverEtudiantParId(etuds.get(0).getId());
                logger.info("[UTILISATEURS] Étudiant par ID {}: semestre={}", etudParId.getId(), etudParId.getSemestreActuel());
            }
            
            // Tests d'activation/désactivation
            if (!admins.isEmpty()) {
                Long adminId = admins.get(0).getId();
                metier.desactiverAdministrateur(adminId);
                logger.info("[UTILISATEURS] Administrateur désactivé: id={}", adminId);
                
                AdministrateurSortieDTO adminDesactive = metier.trouverAdministrateurParId(adminId);
                logger.info("[UTILISATEURS] Vérification désactivation admin: enabled={}", adminDesactive.getEnabled());
                
                metier.activerAdministrateur(adminId);
                logger.info("[UTILISATEURS] Administrateur réactivé: id={}", adminId);
                
                AdministrateurSortieDTO adminReactive = metier.trouverAdministrateurParId(adminId);
                logger.info("[UTILISATEURS] Vérification réactivation admin: enabled={}", adminReactive.getEnabled());
            }
            
            if (!profs.isEmpty()) {
                Long profId = profs.get(0).getId();
                metier.desactiverProfesseur(profId);
                logger.info("[UTILISATEURS] Professeur désactivé: id={}", profId);
                
                ProfesseurSortieDTO profDesactive = metier.trouverProfesseurParId(profId);
                logger.info("[UTILISATEURS] Vérification désactivation prof: enabled={}", profDesactive.getEnabled());
                
                metier.activerProfesseur(profId);
                logger.info("[UTILISATEURS] Professeur réactivé: id={}", profId);
                
                ProfesseurSortieDTO profReactive = metier.trouverProfesseurParId(profId);
                logger.info("[UTILISATEURS] Vérification réactivation prof: enabled={}", profReactive.getEnabled());
            }
            
            if (!etuds.isEmpty()) {
                Long etudId = etuds.get(0).getId();
                metier.desactiverEtudiant(etudId);
                logger.info("[UTILISATEURS] Étudiant désactivé: id={}", etudId);
                
                EtudiantSortieDTO etudDesactive = metier.trouverEtudiantParId(etudId);
                logger.info("[UTILISATEURS] Vérification désactivation étudiant: enabled={}", etudDesactive.getEnabled());
                
                metier.activerEtudiant(etudId);
                logger.info("[UTILISATEURS] Étudiant réactivé: id={}", etudId);
                
                EtudiantSortieDTO etudReactive = metier.trouverEtudiantParId(etudId);
                logger.info("[UTILISATEURS] Vérification réactivation étudiant: enabled={}", etudReactive.getEnabled());
            }
            
        } catch (UtilisateursException e) {
            logger.error("[UTILISATEURS] ERREUR dans testOtherUtilisateurs: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[UTILISATEURS] ERREUR INATTENDUE dans testOtherUtilisateurs: {}", e.getMessage(), e);
        }
    }
}
