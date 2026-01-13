package com.mycampus.backend;

import com.mycampus.backend.dto.administrateur.DonneSortie.*;
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

import java.util.List;

@SpringBootApplication
public class BackendApplication {

  //  private static final Logger logger = LoggerFactory.getLogger(BackendApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    //@Bean
    /*CommandLineRunner testMetierLayers(
        GestionStructureAcademiqueMetier gestionStructureAcademiqueMetier,
        GestionUtilisateursMetier gestionUtilisateursMetier
    ) {
        return args -> {
      //  logger.info("=== DÉMARRAGE DES TESTS DES MÉTIERS ===");
            
          //  Test 1: Gestion Structure Académique
           testStructureAcademique(gestionStructureAcademiqueMetier);
            
           //  Test 2: Gestion Utilisateurs
              testUtilisateurs(gestionUtilisateursMetier);
            
         //   logger.info("=== FIN DES TESTS DES MÉTIERS ===");
            logger.info("=== DÉMARRAGE DES TESTS DE Affectation ===");
           AffectationEnseignementSortieDTO affectationEnseignementSortieDTO = AffectationEnseignementSortieDTO.builder()
                   .anneeUniversitaire("2025/2026")
                   .semestre(Semestre.S1)
                   .matiereId(1L)
                   .professeurPpr("87654321")
                   .classeId(1L)
                   .build();
         //  gestionStructureAcademiqueMetier.creerAffectationEnseignement(affectationEnseignementDTO);
            AffectationEnseignementSortieDTO affectationEnseignementSortieDTO1 = AffectationEnseignementSortieDTO.builder()
                    .anneeUniversitaire("2026/2027")
                    .semestre(Semestre.S2)
                    .matiereId(1L)
                    .professeurPpr("87654321")
                    .classeId(1L)
                    .build();
         //   gestionStructureAcademiqueMetier.modifierAffectationEnseignement(1L, affectationEnseignementDTO1);

            gestionStructureAcademiqueMetier.desactiverAffectationEnseignement(1L);
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
            DepartementSortieDTO departementSortieDTO = DepartementSortieDTO.builder()
                .codeDepartement("INFO")
                .typeDepartement("Informatique")
                .description("Département d'informatique")
                .build();
            
            DepartementSortieDTO departementCree = metier.creerDepartement(departementSortieDTO);
            departementId = departementCree.getId();
            logger.info("[STRUCTURE] Création département OK: id={}, code={}", departementCree.getId(), departementCree.getCodeDepartement());
            
            // Créer une filière liée au département
            FiliereSortieDTO filiereSortieDTO = FiliereSortieDTO.builder()
                .codeFiliere("ILCS")
                .nom("Informatique Licence Cyber Sécurité")
                .description("Formation en cybersécurité")
                .dureeFormation("3 Ans")
                .niveauDiplome("Licence")
                .etat(EtatStructure.OUVERTE)
                .departementId(departementCree.getId())
                .build();
            
            FiliereSortieDTO filiereCreee = metier.creerFiliere(filiereSortieDTO);
            filiereId = filiereCreee.getId();
            logger.info("[STRUCTURE] Création filière OK: id={}, nom={}", filiereCreee.getId(), filiereCreee.getNom());
            
            // Créer une classe liée à la filière
            ClasseSortieDTO classeSortieDTO = ClasseSortieDTO.builder()
                .codeClasse("ILCS1")
                .nom("ILCS Semestre 1")
                .niveau("1ère année")
                .anneeUniversitaire("2025/2026")
                .effectifMax(30)
                .filiereId(filiereCreee.getId())
                .build();
            
            ClasseSortieDTO classeCree = metier.creerClasse(classeSortieDTO);
            classeId = classeCree.getId();
            logger.info("[STRUCTURE] Création classe OK: id={}, nom={}", classeCree.getId(), classeCree.getNom());
            
            // Créer un module lié à la filière
            ModuleSortieDTO moduleSortieDTO = ModuleSortieDTO.builder()
                .codeModule("MATH101")
                .nom("Mathématiques Fondamentales")
                .description("Module de mathématiques de base")
                .semestre(Semestre.S1)
                .coefficient(3)
                .filiereId(filiereCreee.getId())
                .build();
            
            ModuleSortieDTO moduleCree = metier.creerModule(moduleSortieDTO);
            moduleId = moduleCree.getId();
            logger.info("[STRUCTURE] Création module OK: id={}, nom={}", moduleCree.getId(), moduleCree.getNom());
            
            // Créer une matière liée au module
            MatiereSortieDTO matiereSortieDTO = MatiereSortieDTO.builder()
                .codeMatiere("ALGEBRE")
                .nom("Algèbre Linéaire")
                .description("Cours d'algèbre linéaire")
                .coefficient(2)
                .moduleId(moduleCree.getId())
                .build();
            
            MatiereSortieDTO matiereCree = metier.creerMatiere(matiereSortieDTO);
            matiereId = matiereCree.getId();
            logger.info("[STRUCTURE] Création matière OK: id={}, nom={}", matiereCree.getId(), matiereCree.getNom());
            
            // MISE À JOUR
            logger.info("[STRUCTURE] === MISE À JOUR DES ENTITÉS ===");
            
            // Mettre à jour le département
            DepartementSortieDTO departementUpdate = DepartementSortieDTO.builder()
                .id(departementId)
                .codeDepartement("INFO")
                .typeDepartement("Informatique et Réseaux")
                .description("Département d'informatique et réseaux")
                .build();
            
            String ancienneDescription = departementCree.getDescription();
            DepartementSortieDTO departementModifie = metier.modifierDepartement(departementId, departementUpdate);
            logger.info("[STRUCTURE] Mise à jour département OK: id={}, descriptionAvant={}, descriptionApres={}", 
                departementModifie.getId(), ancienneDescription, departementModifie.getDescription());
            
            // Mettre à jour la filière
            FiliereSortieDTO filiereUpdate = FiliereSortieDTO.builder()
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
            FiliereSortieDTO filiereModifiee = metier.modifierFiliere(filiereId, filiereUpdate);
            logger.info("[STRUCTURE] Mise à jour filière OK: id={}, nomAvant={}, nomApres={}", 
                filiereModifiee.getId(), ancienNomFiliere, filiereModifiee.getNom());
            
            // Mettre à jour la classe
            ClasseSortieDTO classeUpdate = ClasseSortieDTO.builder()
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
            ClasseSortieDTO classeModifiee = metier.modifierClasse(classeId, classeUpdate);
            logger.info("[STRUCTURE] Mise à jour classe OK: id={}, nomAvant={}, nomApres={}, effectifAvant={}, effectifApres={}", 
                classeModifiee.getId(), ancienNomClasse, classeModifiee.getNom(), ancienEffectif, classeModifiee.getEffectifMax());
            
            // Mettre à jour le module
            ModuleSortieDTO moduleUpdate = ModuleSortieDTO.builder()
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
            ModuleSortieDTO moduleModifie = metier.modifierModule(moduleId, moduleUpdate);
            logger.info("[STRUCTURE] Mise à jour module OK: id={}, nomAvant={}, nomApres={}, coefficientAvant={}, coefficientApres={}", 
                moduleModifie.getId(), ancienNomModule, moduleModifie.getNom(), ancienCoefficient, moduleModifie.getCoefficient());
            
            // Mettre à jour la matière
            MatiereSortieDTO matiereUpdate = MatiereSortieDTO.builder()
                .id(matiereId)
                .codeMatiere("ALGEBRE")
                .nom("Algèbre Linéaire Avancée")
                .description("Cours avancé d'algèbre linéaire")
                .coefficient(3)
                .moduleId(moduleId)
                .build();
            
            String ancienNomMatiere = matiereCree.getNom();
            Integer ancienCoefficientMatiere = matiereCree.getCoefficient();
            MatiereSortieDTO matiereModifiee = metier.modifierMatiere(matiereId, matiereUpdate);
            logger.info("[STRUCTURE] Mise à jour matière OK: id={}, nomAvant={}, nomApres={}, coefficientAvant={}, coefficientApres={}", 
                matiereModifiee.getId(), ancienNomMatiere, matiereModifiee.getNom(), ancienCoefficientMatiere, matiereModifiee.getCoefficient());
            
            // VÉRIFICATION DES MISES À JOUR
            logger.info("[STRUCTURE] === VÉRIFICATION DES MISES À JOUR ===");
            
            DepartementSortieDTO departementVerifie = metier.trouverDepartementParId(departementId);
            logger.info("[STRUCTURE] Vérification département: description={}", departementVerifie.getDescription());
            
            FiliereSortieDTO filiereVerifiee = metier.trouverFiliereParId(filiereId);
            logger.info("[STRUCTURE] Vérification filière: nom={}", filiereVerifiee.getNom());
            
            ClasseSortieDTO classeVerifiee = metier.trouverClasseParId(classeId);
            logger.info("[STRUCTURE] Vérification classe: nom={}, effectifMax={}", classeVerifiee.getNom(), classeVerifiee.getEffectifMax());
            
            ModuleSortieDTO moduleVerifie = metier.trouverModuleParId(moduleId);
            logger.info("[STRUCTURE] Vérification module: nom={}, coefficient={}", moduleVerifie.getNom(), moduleVerifie.getCoefficient());
            
            MatiereSortieDTO matiereVerifiee = metier.trouverMatiereParId(matiereId);
            logger.info("[STRUCTURE] Vérification matière: nom={}, coefficient={}", matiereVerifiee.getNom(), matiereVerifiee.getCoefficient());
  
     /*       // SUPPRESSION
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
            
            AdministrateurSortieDTO administrateurSortieDTO = AdministrateurSortieDTO.builder()
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
            
            AdministrateurSortieDTO adminCree = metier.creerAdministrateur(administrateurSortieDTO);
            adminId = adminCree.getId();
            logger.info("[UTILISATEURS] Création administrateur OK: id={}, prenom={}, nom={}", 
                adminCree.getId(), adminCree.getPrenom(), adminCree.getNom());
            
            // VÉRIFICATION CRÉATION
            logger.info("[UTILISATEURS] === VÉRIFICATION CRÉATION ===");
            
            AdministrateurSortieDTO adminParPpr = metier.trouverAdministrateurParPpr("12345678");
            if (adminParPpr != null) {
                logger.info("[UTILISATEURS] Vérification par PPR OK: email={}", adminParPpr.getEmail());
            }
            
            List<AdministrateurSortieDTO> listeAdmins = metier.listerTousLesAdministrateurs();
            logger.info("[UTILISATEURS] Total administrateurs: {}", listeAdmins.size());
            
            // MISE À JOUR
            logger.info("[UTILISATEURS] === MISE À JOUR ADMINISTRATEUR ===");
            
            AdministrateurSortieDTO adminUpdate = AdministrateurSortieDTO.builder()
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
            
            AdministrateurSortieDTO adminModifie = metier.modifierAdministrateur(adminId, adminUpdate);
            logger.info("[UTILISATEURS] Mise à jour administrateur OK: id={}, fonctionAvant={}, fonctionApres={}, gradeAvant={}, gradeApres={}, emailAvant={}, emailApres={}", 
                adminModifie.getId(), ancienneFonction, adminModifie.getFonction(), ancienGrade, adminModifie.getGrade(), ancienEmail, adminModifie.getEmail());
            
            // VÉRIFICATION MISE À JOUR
            logger.info("[UTILISATEURS] === VÉRIFICATION MISE À JOUR ===");
            
            AdministrateurSortieDTO adminVerifie = metier.trouverAdministrateurParId(adminId);
            logger.info("[UTILISATEURS] Vérification mise à jour: fonction={}, grade={}, email={}", 
                adminVerifie.getFonction(), adminVerifie.getGrade(), adminVerifie.getEmail());
            
            // ACTIVATION/DÉSACTIVATION
            logger.info("[UTILISATEURS] === TEST ACTIVATION/DÉSACTIVATION ===");
            
            metier.desactiverAdministrateur(adminId);
            logger.info("[UTILISATEURS] Administrateur désactivé: id={}", adminId);
            
            // Vérifier désactivation
            AdministrateurSortieDTO adminDesactive = metier.trouverAdministrateurParId(adminId);
            logger.info("[UTILISATEURS] Vérification désactivation: enabled={}", adminDesactive.getEnabled());
            
            metier.activerAdministrateur(adminId);
            logger.info("[UTILISATEURS] Administrateur réactivé: id={}", adminId);
            
            // Vérifier réactivation
            AdministrateurSortieDTO adminReactive = metier.trouverAdministrateurParId(adminId);
            logger.info("[UTILISATEURS] Vérification réactivation: enabled={}", adminReactive.getEnabled());
            
            // SUPPRESSION
            logger.info("[UTILISATEURS] === SUPPRESSION ADMINISTRATEUR ===");
            
            try {
                metier.supprimerAdministrateur(adminId);
                logger.info("[UTILISATEURS] Suppression administrateur confirmée: id={}", adminId);
                
                metier.trouverAdministrateurParId(adminId);
                logger.warn("[UTILISATEURS] L'administrateur n'a pas été supprimé correctement");
            } catch (UtilisateursException e) {
                logger.info("[UTILISATEURS] Suppression administrateur confirmée: id={} (non trouvé)", adminId);
            }
            
            // ==================== TEST PROFESSEUR ====================
            logger.info("[UTILISATEURS] === CRÉATION PROFESSEUR ===");
            
            Long profId = null;
            
            ProfesseurSortieDTO professeurSortieDTO = ProfesseurSortieDTO.builder()
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
                .dateRecrutement(java.time.LocalDate.of(2020, 9, 1))
                // Relations optionnelles - mettre à null pour éviter les dépendances
                .departementDirigeeId(null)
                .departementOuTravailId(null)
                .filiereDirigeeId(null)
                .build();
            
            ProfesseurSortieDTO profCree = metier.creerProfesseur(professeurSortieDTO);
            profId = profCree.getId();
            logger.info("[UTILISATEURS] Création professeur OK: id={}, prenom={}, nom={}, specialite={}", 
                profCree.getId(), profCree.getPrenom(), profCree.getNom(), profCree.getSpecialite());
            
            // VÉRIFICATION CRÉATION PROFESSEUR
            logger.info("[UTILISATEURS] === VÉRIFICATION CRÉATION PROFESSEUR ===");
            
            ProfesseurSortieDTO profParPpr = metier.trouverProfesseurParPpr("87654321");
            if (profParPpr != null) {
                logger.info("[UTILISATEURS] Vérification professeur par PPR OK: email={}, grade={}", 
                    profParPpr.getEmail(), profParPpr.getGrade());
            }
            
            List<ProfesseurSortieDTO> listeProfs = metier.listerTousLesProfesseurs();
            logger.info("[UTILISATEURS] Total professeurs: {}", listeProfs.size());
            
            // MISE À JOUR PROFESSEUR
            logger.info("[UTILISATEURS] === MISE À JOUR PROFESSEUR ===");
            
            ProfesseurSortieDTO profUpdate = ProfesseurSortieDTO.builder()
                .id(profId)

                .ppr("87654321")
                .nom("Martin")
                .prenom("Pierre")
                .email("pierre.martin.modifie@mycampus.ma")
                .cin("BB987654")
                .tel("0623456789")
                .specialite("Intelligence Artificielle")
                .grade("PH")
                .statut("Titulaire")
                .dateRecrutement(java.time.LocalDate.of(2020, 9, 1))
                .departementDirigeeId(null)
                .departementOuTravailId(null)
                .filiereDirigeeId(null)

                                    .build();
            
            String ancienneSpecialite = profCree.getSpecialite();
            String ancienGrades = profCree.getGrade();
            String ancienEmails = profCree.getEmail();
            
            ProfesseurSortieDTO profModifie = metier.modifierProfesseur(profId, profUpdate);
            logger.info("[UTILISATEURS] Mise à jour professeur OK: id={}, specialiteAvant={}, specialiteApres={}, gradeAvant={}, gradeApres={}, emailAvant={}, emailApres={}", 
                profModifie.getId(), ancienneSpecialite, profModifie.getSpecialite(), ancienGrades, profModifie.getGrade(), ancienEmails, profModifie.getEmail());
            
            // VÉRIFICATION MISE À JOUR PROFESSEUR
            logger.info("[UTILISATEURS] === VÉRIFICATION MISE À JOUR PROFESSEUR ===");
            
            ProfesseurSortieDTO profVerifie = metier.trouverProfesseurParId(profId);
            logger.info("[UTILISATEURS] Vérification mise à jour professeur: specialite={}, grade={}, email={}", 
                profVerifie.getSpecialite(), profVerifie.getGrade(), profVerifie.getEmail());
            
            // ACTIVATION/DÉSACTIVATION PROFESSEUR
            logger.info("[UTILISATEURS] === TEST ACTIVATION/DÉSACTIVATION PROFESSEUR ===");
            
            metier.desactiverProfesseur(profId);
            logger.info("[UTILISATEURS] Professeur désactivé: id={}", profId);
            
            // Vérifier désactivation
            ProfesseurSortieDTO profDesactive = metier.trouverProfesseurParId(profId);
            logger.info("[UTILISATEURS] Vérification désactivation professeur: enabled={}", profDesactive.getEnabled());
            
            metier.activerProfesseur(profId);
            logger.info("[UTILISATEURS] Professeur réactivé: id={}", profId);
            
            // Vérifier réactivation
            ProfesseurSortieDTO profReactive = metier.trouverProfesseurParId(profId);
            logger.info("[UTILISATEURS] Vérification réactivation professeur: enabled={}", profReactive.getEnabled());
            
            // SUPPRESSION PROFESSEUR
     logger.info("[UTILISATEURS] === SUPPRESSION PROFESSEUR ===");
            
            try {
                metier.supprimerProfesseur(profId);
                logger.info("[UTILISATEURS] Suppression professeur confirmée: id={}", profId);
                
                // Vérifier la suppression
                metier.trouverProfesseurParId(profId);
                logger.warn("[UTILISATEURS] Le professeur n'a pas été supprimé correctement");
            } catch (UtilisateursException e) {
                logger.info("[UTILISATEURS] Suppression professeur confirmée: id={} (non trouvé)", profId);
            }
            
            // ==================== TEST ETUDIANT ====================
            logger.info("[UTILISATEURS] === CRÉATION ETUDIANT ===");
            
            Long etudiantId = null;
            
            EtudiantSortieDTO etudiantSortieDTO = EtudiantSortieDTO.builder()
                .apogee("202300123")
                .nom("Bernard")
                .prenom("Sophie")
                .email("sophie.bernard@mycampus.ma")
                .cin("CC123789")
                .password("password789")
                .tel("0634567890")
                .niveau("3ème année")
                .anneeInscription(2023)
                .semestreActuel(Semestre.S5)
                .dateNaissance(java.time.LocalDate.of(2002, 5, 15))
                .etatInscription(EtatInscription.INSCRIT)
                // Classe optionnelle - mettre à null pour éviter les dépendances
                .classeId(1L)
                .build();
            
            EtudiantSortieDTO etudiantCree = metier.creerEtudiant(etudiantSortieDTO);
            etudiantId = etudiantCree.getId();
            logger.info("[UTILISATEURS] Création étudiant OK: id={}, prenom={}, nom={}, apogee={}, niveau={}", 
                etudiantCree.getId(), etudiantCree.getPrenom(), etudiantCree.getNom(), 
                etudiantCree.getApogee(), etudiantCree.getNiveau());
            
            // VÉRIFICATION CRÉATION ETUDIANT
            logger.info("[UTILISATEURS] === VÉRIFICATION CRÉATION ETUDIANT ===");
            
            EtudiantSortieDTO etudiantParApogee = metier.trouverEtudiantParApogee("202300123");
            if (etudiantParApogee != null) {
                logger.info("[UTILISATEURS] Vérification étudiant par Apogée OK: email={}, semestre={}", 
                    etudiantParApogee.getEmail(), etudiantParApogee.getSemestreActuel());
            }
            
            List<EtudiantSortieDTO> listeEtudiants = metier.listerTousLesEtudiants();
            logger.info("[UTILISATEURS] Total étudiants: {}", listeEtudiants.size());
            
            // MISE À JOUR ETUDIANT
            logger.info("[UTILISATEURS] === MISE À JOUR ETUDIANT ===");
            
            EtudiantSortieDTO etudiantUpdate = EtudiantSortieDTO.builder()
                .id(etudiantId)
                .apogee("202300123")
                .nom("Bernard")
                .prenom("Sophie")
                .email("sophie.bernard.modifie@mycampus.ma")
                .cin("CC123789")
                .tel("0634567890")
                .niveau("4ème année")
                .anneeInscription(2023)
                .semestreActuel(com.mycampus.backend.enumerations.Semestre.S2)
                .dateNaissance(java.time.LocalDate.of(2002, 5, 15))
                .etatInscription(EtatInscription.INSCRIT)
                .classeId(null)
                .build();
            
            String ancienNiveau = etudiantCree.getNiveau();
            Semestre ancienSemestre = etudiantCree.getSemestreActuel();
            String ancienEmailEtudiant = etudiantCree.getEmail();
            
            EtudiantSortieDTO etudiantModifie = metier.modifierEtudiant(etudiantId, etudiantUpdate);
            logger.info("[UTILISATEURS] Mise à jour étudiant OK: id={}, niveauAvant={}, niveauApres={}, semestreAvant={}, semestreApres={}, emailAvant={}, emailApres={}", 
                etudiantModifie.getId(), ancienNiveau, etudiantModifie.getNiveau(), 
                ancienSemestre, etudiantModifie.getSemestreActuel(), 
                ancienEmailEtudiant, etudiantModifie.getEmail());
            
            // VÉRIFICATION MISE À JOUR ETUDIANT
            logger.info("[UTILISATEURS] === VÉRIFICATION MISE À JOUR ETUDIANT ===");
            
            EtudiantSortieDTO etudiantVerifie = metier.trouverEtudiantParId(etudiantId);
            logger.info("[UTILISATEURS] Vérification mise à jour étudiant: niveau={}, semestre={}, email={}", 
                etudiantVerifie.getNiveau(), etudiantVerifie.getSemestreActuel(), etudiantVerifie.getEmail());
            
            // ACTIVATION/DÉSACTIVATION ETUDIANT
            logger.info("[UTILISATEURS] === TEST ACTIVATION/DÉSACTIVATION ETUDIANT ===");
            
            metier.desactiverEtudiant(etudiantId);
            logger.info("[UTILISATEURS] Étudiant désactivé: id={}", etudiantId);
            
            // Vérifier désactivation
            EtudiantSortieDTO etudiantDesactive = metier.trouverEtudiantParId(etudiantId);
            logger.info("[UTILISATEURS] Vérification désactivation étudiant: enabled={}", etudiantDesactive.getEnabled());
            
            metier.activerEtudiant(etudiantId);
            logger.info("[UTILISATEURS] Étudiant réactivé: id={}", etudiantId);
            
            // Vérifier réactivation
            EtudiantSortieDTO etudiantReactive = metier.trouverEtudiantParId(etudiantId);
            logger.info("[UTILISATEURS] Vérification réactivation étudiant: enabled={}", etudiantReactive.getEnabled());
            
            // SUPPRESSION ETUDIANT
            logger.info("[UTILISATEURS] === SUPPRESSION ETUDIANT ===");
            
            try {
                metier.supprimerEtudiant(etudiantId);
                logger.info("[UTILISATEURS] Suppression étudiant confirmée: id={}", etudiantId);
                
                // Vérifier la suppression
                metier.trouverEtudiantParId(etudiantId);
                logger.warn("[UTILISATEURS] L'étudiant n'a pas été supprimé correctement");
            } catch (UtilisateursException e) {
                logger.info("[UTILISATEURS] Suppression étudiant confirmée: id={} (non trouvé)", etudiantId);
            }
            
            logger.info("[UTILISATEURS] --- TEST GESTION UTILISATEURS (ADMIN + PROF + ETUDIANT) TERMINÉ AVEC SUCCÈS ---");
            
        } catch (UtilisateursException e) {
            logger.error("[UTILISATEURS] ERREUR dans testUtilisateurs: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[UTILISATEURS] ERREUR INATTENDUE dans testUtilisateurs: {}", e.getMessage(), e);
        }
    }*/
}