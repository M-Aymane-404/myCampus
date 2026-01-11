package com.mycampus.backend;

import com.mycampus.backend.dto.DepartementDTO;
import com.mycampus.backend.dto.FiliereDTO;
import com.mycampus.backend.dto.ClasseDTO;
import com.mycampus.backend.dto.ModuleDTO;
import com.mycampus.backend.dto.MatiereDTO;
import com.mycampus.backend.dto.AdministrateurDTO;
import com.mycampus.backend.enumerations.EtatStructure;
import com.mycampus.backend.enumerations.Semestre;
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
            
            // Test 1: Gestion Structure Académique
            testStructureAcademique(gestionStructureAcademiqueMetier);
            
            // Test 2: Gestion Utilisateurs
            testUtilisateurs(gestionUtilisateursMetier);
            
            logger.info("=== FIN DES TESTS DES MÉTIERS ===");
        };
    }

    private void testStructureAcademique(GestionStructureAcademiqueMetier metier) {
        logger.info("--- TEST GESTION STRUCTURE ACADÉMIQUE ---");
        
        Long departementId = null;
        Long filiereId = null;
        Long classeId = null;
        Long moduleId = null;
        Long matiereId = null;
        
        try {
            // CRÉATION
            logger.info("[STRUCTURE] === CRÉATION DES ENTITÉS ===");
            
            // Créer un département
            DepartementDTO departementDTO = DepartementDTO.builder()
                .codeDepartement("INFO")
                .typeDepartement("Informatique")
                .description("Département d'informatique")
                .build();
            
            DepartementDTO departementCree = metier.creerDepartement(departementDTO);
            departementId = departementCree.getId();
            logger.info("[STRUCTURE] Création département OK: id={}, code={}", departementCree.getId(), departementCree.getCodeDepartement());
            
            // Créer une filière liée au département
            FiliereDTO filiereDTO = FiliereDTO.builder()
                .codeFiliere("ILCS")
                .nom("Informatique Licence Cyber Sécurité")
                .description("Formation en cybersécurité")
                .dureeFormation("3 Ans")
                .niveauDiplome("Licence")
                .etat(EtatStructure.OUVERTE)
                .departementId(departementCree.getId())
                .build();
            
            FiliereDTO filiereCreee = metier.creerFiliere(filiereDTO);
            filiereId = filiereCreee.getId();
            logger.info("[STRUCTURE] Création filière OK: id={}, nom={}", filiereCreee.getId(), filiereCreee.getNom());
            
            // Créer une classe liée à la filière
            ClasseDTO classeDTO = ClasseDTO.builder()
                .codeClasse("ILCS1")
                .nom("ILCS Semestre 1")
                .niveau("1ère année")
                .anneeUniversitaire("2025/2026")
                .effectifMax(30)
                .filiereId(filiereCreee.getId())
                .build();
            
            ClasseDTO classeCree = metier.creerClasse(classeDTO);
            classeId = classeCree.getId();
            logger.info("[STRUCTURE] Création classe OK: id={}, nom={}", classeCree.getId(), classeCree.getNom());
            
            // Créer un module lié à la filière
            ModuleDTO moduleDTO = ModuleDTO.builder()
                .codeModule("MATH101")
                .nom("Mathématiques Fondamentales")
                .description("Module de mathématiques de base")
                .semestre(Semestre.S1)
                .coefficient(3)
                .filiereId(filiereCreee.getId())
                .build();
            
            ModuleDTO moduleCree = metier.creerModule(moduleDTO);
            moduleId = moduleCree.getId();
            logger.info("[STRUCTURE] Création module OK: id={}, nom={}", moduleCree.getId(), moduleCree.getNom());
            
            // Créer une matière liée au module
            MatiereDTO matiereDTO = MatiereDTO.builder()
                .codeMatiere("ALGEBRE")
                .nom("Algèbre Linéaire")
                .description("Cours d'algèbre linéaire")
                .coefficient(2)
                .moduleId(moduleCree.getId())
                .build();
            
            MatiereDTO matiereCree = metier.creerMatiere(matiereDTO);
            matiereId = matiereCree.getId();
            logger.info("[STRUCTURE] Création matière OK: id={}, nom={}", matiereCree.getId(), matiereCree.getNom());
            
            // MISE À JOUR
            logger.info("[STRUCTURE] === MISE À JOUR DES ENTITÉS ===");
            
            // Mettre à jour le département
            DepartementDTO departementUpdate = DepartementDTO.builder()
                .id(departementId)
                .codeDepartement("INFO")
                .typeDepartement("Informatique et Réseaux")
                .description("Département d'informatique et réseaux")
                .build();
            
            String ancienneDescription = departementCree.getDescription();
            DepartementDTO departementModifie = metier.modifierDepartement(departementId, departementUpdate);
            logger.info("[STRUCTURE] Mise à jour département OK: id={}, descriptionAvant={}, descriptionApres={}", 
                departementModifie.getId(), ancienneDescription, departementModifie.getDescription());
            
            // Mettre à jour la filière
            FiliereDTO filiereUpdate = FiliereDTO.builder()
                .id(filiereId)
                .codeFiliere("ILCS")
                .nom("Informatique Licence Cybersécurité Spécialisée")
                .description("Formation spécialisée en cybersécurité")
                .dureeFormation("3 Ans")
                .niveauDiplome("Licence")
                .etat(EtatStructure.OUVERTE)
                .departementId(departementId)
                .build();
            
            String ancienNomFiliere = filiereCreee.getNom();
            FiliereDTO filiereModifiee = metier.modifierFiliere(filiereId, filiereUpdate);
            logger.info("[STRUCTURE] Mise à jour filière OK: id={}, nomAvant={}, nomApres={}", 
                filiereModifiee.getId(), ancienNomFiliere, filiereModifiee.getNom());
            
            // Mettre à jour la classe
            ClasseDTO classeUpdate = ClasseDTO.builder()
                .id(classeId)
                .codeClasse("ILCS1")
                .nom("ILCS Semestre 1 - Groupe A")
                .niveau("1ère année")
                .anneeUniversitaire("2025/2026")
                .effectifMax(35)
                .filiereId(filiereId)
                .build();
            
            String ancienNomClasse = classeCree.getNom();
            Integer ancienEffectif = classeCree.getEffectifMax();
            ClasseDTO classeModifiee = metier.modifierClasse(classeId, classeUpdate);
            logger.info("[STRUCTURE] Mise à jour classe OK: id={}, nomAvant={}, nomApres={}, effectifAvant={}, effectifApres={}", 
                classeModifiee.getId(), ancienNomClasse, classeModifiee.getNom(), ancienEffectif, classeModifiee.getEffectifMax());
            
            // Mettre à jour le module
            ModuleDTO moduleUpdate = ModuleDTO.builder()
                .id(moduleId)
                .codeModule("MATH101")
                .nom("Mathématiques Appliquées")
                .description("Module de mathématiques appliquées")
                .semestre(Semestre.S1)
                .coefficient(4)
                .filiereId(filiereId)
                .build();
            
            String ancienNomModule = moduleCree.getNom();
            Integer ancienCoefficient = moduleCree.getCoefficient();
            ModuleDTO moduleModifie = metier.modifierModule(moduleId, moduleUpdate);
            logger.info("[STRUCTURE] Mise à jour module OK: id={}, nomAvant={}, nomApres={}, coefficientAvant={}, coefficientApres={}", 
                moduleModifie.getId(), ancienNomModule, moduleModifie.getNom(), ancienCoefficient, moduleModifie.getCoefficient());
            
            // Mettre à jour la matière
            MatiereDTO matiereUpdate = MatiereDTO.builder()
                .id(matiereId)
                .codeMatiere("ALGEBRE")
                .nom("Algèbre Linéaire Avancée")
                .description("Cours avancé d'algèbre linéaire")
                .coefficient(3)
                .moduleId(moduleId)
                .build();
            
            String ancienNomMatiere = matiereCree.getNom();
            Integer ancienCoefficientMatiere = matiereCree.getCoefficient();
            MatiereDTO matiereModifiee = metier.modifierMatiere(matiereId, matiereUpdate);
            logger.info("[STRUCTURE] Mise à jour matière OK: id={}, nomAvant={}, nomApres={}, coefficientAvant={}, coefficientApres={}", 
                matiereModifiee.getId(), ancienNomMatiere, matiereModifiee.getNom(), ancienCoefficientMatiere, matiereModifiee.getCoefficient());
            
            // VÉRIFICATION DES MISES À JOUR
            logger.info("[STRUCTURE] === VÉRIFICATION DES MISES À JOUR ===");
            
            DepartementDTO departementVerifie = metier.trouverDepartementParId(departementId);
            logger.info("[STRUCTURE] Vérification département: description={}", departementVerifie.getDescription());
            
            FiliereDTO filiereVerifiee = metier.trouverFiliereParId(filiereId);
            logger.info("[STRUCTURE] Vérification filière: nom={}", filiereVerifiee.getNom());
            
            ClasseDTO classeVerifiee = metier.trouverClasseParId(classeId);
            logger.info("[STRUCTURE] Vérification classe: nom={}, effectifMax={}", classeVerifiee.getNom(), classeVerifiee.getEffectifMax());
            
            ModuleDTO moduleVerifie = metier.trouverModuleParId(moduleId);
            logger.info("[STRUCTURE] Vérification module: nom={}, coefficient={}", moduleVerifie.getNom(), moduleVerifie.getCoefficient());
            
            MatiereDTO matiereVerifiee = metier.trouverMatiereParId(matiereId);
            logger.info("[STRUCTURE] Vérification matière: nom={}, coefficient={}", matiereVerifiee.getNom(), matiereVerifiee.getCoefficient());
            
            // SUPPRESSION
            logger.info("[STRUCTURE] === SUPPRESSION DES ENTITÉS ===");
            
            // Supprimer la matière
            try {
                metier.supprimerMatiere(matiereId);
                logger.info("[STRUCTURE] Suppression matière confirmée: id={}", matiereId);
                
                // Vérifier la suppression
                metier.trouverMatiereParId(matiereId);
                logger.warn("[STRUCTURE] La matière n'a pas été supprimée correctement");
            } catch (StructureAcademiqueException e) {
                logger.info("[STRUCTURE] Suppression matière confirmée: id={} (non trouvée)", matiereId);
            }
            
            // Supprimer le module
            try {
                metier.supprimerModule(moduleId);
                logger.info("[STRUCTURE] Suppression module confirmée: id={}", moduleId);
                
                // Vérifier la suppression
                metier.trouverModuleParId(moduleId);
                logger.warn("[STRUCTURE] Le module n'a pas été supprimé correctement");
            } catch (StructureAcademiqueException e) {
                logger.info("[STRUCTURE] Suppression module confirmée: id={} (non trouvé)", moduleId);
            }
            
            // Supprimer la classe
            try {
                metier.supprimerClasse(classeId);
                logger.info("[STRUCTURE] Suppression classe confirmée: id={}", classeId);
                
                // Vérifier la suppression
                metier.trouverClasseParId(classeId);
                logger.warn("[STRUCTURE] La classe n'a pas été supprimée correctement");
            } catch (StructureAcademiqueException e) {
                logger.info("[STRUCTURE] Suppression classe confirmée: id={} (non trouvée)", classeId);
            }
            
            // Supprimer la filière
            try {
                metier.supprimerFiliere(filiereId);
                logger.info("[STRUCTURE] Suppression filière confirmée: id={}", filiereId);
                
                // Vérifier la suppression
                metier.trouverFiliereParId(filiereId);
                logger.warn("[STRUCTURE] La filière n'a pas été supprimée correctement");
            } catch (StructureAcademiqueException e) {
                logger.info("[STRUCTURE] Suppression filière confirmée: id={} (non trouvée)", filiereId);
            }
            
            // Supprimer le département
            try {
                metier.supprimerDepartement(departementId);
                logger.info("[STRUCTURE] Suppression département confirmée: id={}", departementId);
                
                // Vérifier la suppression
                metier.trouverDepartementParId(departementId);
                logger.warn("[STRUCTURE] Le département n'a pas été supprimé correctement");
            } catch (StructureAcademiqueException e) {
                logger.info("[STRUCTURE] Suppression département confirmée: id={} (non trouvé)", departementId);
            }
            
            logger.info("[STRUCTURE] --- TEST STRUCTURE ACADÉMIQUE TERMINÉ AVEC SUCCÈS ---");
            
        } catch (StructureAcademiqueException e) {
            logger.error("[STRUCTURE] ERREUR dans testStructureAcademique: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[STRUCTURE] ERREUR INATTENDUE dans testStructureAcademique: {}", e.getMessage(), e);
        }
    }

    private void testUtilisateurs(GestionUtilisateursMetier metier) {
        logger.info("--- TEST GESTION UTILISATEURS ---");
        
        Long adminId = null;
        
        try {
            // CRÉATION
            logger.info("[UTILISATEURS] === CRÉATION ADMINISTRATEUR ===");
            
            AdministrateurDTO administrateurDTO = AdministrateurDTO.builder()
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
            
            AdministrateurDTO adminCree = metier.creerAdministrateur(administrateurDTO);
            adminId = adminCree.getId();
            logger.info("[UTILISATEURS] Création administrateur OK: id={}, prenom={}, nom={}", 
                adminCree.getId(), adminCree.getPrenom(), adminCree.getNom());
            
            // VÉRIFICATION CRÉATION
            logger.info("[UTILISATEURS] === VÉRIFICATION CRÉATION ===");
            
            AdministrateurDTO adminParPpr = metier.trouverAdministrateurParPpr("12345678");
            if (adminParPpr != null) {
                logger.info("[UTILISATEURS] Vérification par PPR OK: email={}", adminParPpr.getEmail());
            }
            
            List<AdministrateurDTO> listeAdmins = metier.listerTousLesAdministrateurs();
            logger.info("[UTILISATEURS] Total administrateurs: {}", listeAdmins.size());
            
            // MISE À JOUR
            logger.info("[UTILISATEURS] === MISE À JOUR ADMINISTRATEUR ===");
            
            AdministrateurDTO adminUpdate = AdministrateurDTO.builder()
                .id(adminId)
                .ppr("12345678")
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont.modifie@mycampus.ma")
                .cin("AA123456")
                .tel("0698765432")
                .fonction("Administrateur Principal")
                .grade("A2")
                .build();
            
            String ancienneFonction = adminCree.getFonction();
            String ancienGrade = adminCree.getGrade();
            String ancienEmail = adminCree.getEmail();
            
            AdministrateurDTO adminModifie = metier.modifierAdministrateur(adminId, adminUpdate);
            logger.info("[UTILISATEURS] Mise à jour administrateur OK: id={}, fonctionAvant={}, fonctionApres={}, gradeAvant={}, gradeApres={}, emailAvant={}, emailApres={}", 
                adminModifie.getId(), ancienneFonction, adminModifie.getFonction(), ancienGrade, adminModifie.getGrade(), ancienEmail, adminModifie.getEmail());
            
            // VÉRIFICATION MISE À JOUR
            logger.info("[UTILISATEURS] === VÉRIFICATION MISE À JOUR ===");
            
            AdministrateurDTO adminVerifie = metier.trouverAdministrateurParId(adminId);
            logger.info("[UTILISATEURS] Vérification mise à jour: fonction={}, grade={}, email={}", 
                adminVerifie.getFonction(), adminVerifie.getGrade(), adminVerifie.getEmail());
            
            // ACTIVATION/DÉSACTIVATION
            logger.info("[UTILISATEURS] === TEST ACTIVATION/DÉSACTIVATION ===");
            
            metier.desactiverAdministrateur(adminId);
            logger.info("[UTILISATEURS] Administrateur désactivé: id={}", adminId);
            
            // Vérifier désactivation
            AdministrateurDTO adminDesactive = metier.trouverAdministrateurParId(adminId);
            logger.info("[UTILISATEURS] Vérification désactivation: enabled={}", adminDesactive.getEnabled());
            
            metier.activerAdministrateur(adminId);
            logger.info("[UTILISATEURS] Administrateur réactivé: id={}", adminId);
            
            // Vérifier réactivation
            AdministrateurDTO adminReactive = metier.trouverAdministrateurParId(adminId);
            logger.info("[UTILISATEURS] Vérification réactivation: enabled={}", adminReactive.getEnabled());
            
            // SUPPRESSION
            logger.info("[UTILISATEURS] === SUPPRESSION ADMINISTRATEUR ===");
            
            try {
                metier.supprimerAdministrateur(adminId);
                logger.info("[UTILISATEURS] Suppression administrateur confirmée: id={}", adminId);
                
                // Vérifier la suppression
                metier.trouverAdministrateurParId(adminId);
                logger.warn("[UTILISATEURS] L'administrateur n'a pas été supprimé correctement");
            } catch (UtilisateursException e) {
                logger.info("[UTILISATEURS] Suppression administrateur confirmée: id={} (non trouvé)", adminId);
            }
            
            logger.info("[UTILISATEURS] --- TEST GESTION UTILISATEURS TERMINÉ AVEC SUCCÈS ---");
            
        } catch (UtilisateursException e) {
            logger.error("[UTILISATEURS] ERREUR dans testUtilisateurs: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[UTILISATEURS] ERREUR INATTENDUE dans testUtilisateurs: {}", e.getMessage(), e);
        }
    }
}