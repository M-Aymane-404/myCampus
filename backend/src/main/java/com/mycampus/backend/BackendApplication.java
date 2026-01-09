package com.mycampus.backend;

import com.mycampus.backend.dto.DepartementDTO;
import com.mycampus.backend.dto.FiliereDTO;
import com.mycampus.backend.dto.ClasseDTO;
import com.mycampus.backend.dto.ModuleDTO;
import com.mycampus.backend.dto.MatiereDTO;
import com.mycampus.backend.dto.AdministrateurDTO;

import com.mycampus.backend.enumerations.EtatStructure;
import com.mycampus.backend.exceptions.StructureAcademiqueException;
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
           // testUtilisateurs(gestionUtilisateursMetier);
            
            // Test 3: Tableau de Bord

            logger.info("=== FIN DES TESTS DES MÉTIERS ===");
        };
    }

    private void testStructureAcademique(GestionStructureAcademiqueMetier metier) {
        logger.info("--- TEST GESTION STRUCTURE ACADÉMIQUE ---");
        
        try {
            // Créer un département
            DepartementDTO departementDTO = DepartementDTO.builder()
                .codeDepartement("INFO")
                .typeDepartement("Informatique")
                .description("Département d'informatique")
                .build();
            
            DepartementDTO departementCree = metier.creerDepartement(departementDTO);
            logger.info("Département créé: {} - {}", departementCree.getId(), departementCree.getCodeDepartement());
            
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
            logger.info("Filière créée: {} - {}", filiereCreee.getId(), filiereCreee.getNom());
            
            // Créer une classe liée à la filière
            ClasseDTO classeDTO = ClasseDTO.builder()
                .codeClasse("ILCS1")
                .nom("ILCS Semestre 1")
                .niveau("1ère année")

                .filiereId(filiereCreee.getId())
                .build();
            
            ClasseDTO classeCree = metier.creerClasse(classeDTO);
            logger.info("Classe créée: {} - {}", classeCree.getId(), classeCree.getNom());
            
            // Créer un module lié à la classe
            ModuleDTO moduleDTO = ModuleDTO.builder()
                .codeModule("MATH101")
                .nom("Mathématiques Fondamentales")
                .description("Module de mathématiques de base")
                .semestre("S1")
                .coefficient(3)
                .classeId(classeCree.getId())
                .build();
            
            ModuleDTO moduleCree = metier.creerModule(moduleDTO);
            logger.info("Module créé: {} - {}", moduleCree.getId(), moduleCree.getNom());
            
            // Créer une matière liée au module
            MatiereDTO matiereDTO = MatiereDTO.builder()
                .codeMatiere("ALGEBRE")
                .nom("Algèbre Linéaire")
                .description("Cours d'algèbre linéaire")
                .coefficient(2)
                .moduleId(moduleCree.getId())
                .build();
            
            MatiereDTO matiereCree = metier.creerMatiere(matiereDTO);
            logger.info("Matière créée: {} - {}", matiereCree.getId(), matiereCree.getNom());
            
            // Tester les méthodes de recherche
            DepartementDTO departementTrouve = metier.trouverDepartementParId(departementCree.getId());
            if (departementTrouve != null) {
                logger.info("Département trouvé: {}", departementTrouve.getCodeDepartement());
            }
            
            List<FiliereDTO> filieres = metier.listerToutesLesFilieres();
            logger.info("Nombre total de filières: {}", filieres.size());
            
            List<ClasseDTO> classes = metier.trouverClassesParFiliere(filiereCreee.getId());
            logger.info("Classes pour la filière {}: {}", filiereCreee.getNom(), classes.size());
            
            logger.info("--- TEST STRUCTURE ACADÉMIQUE TERMINÉ AVEC SUCCÈS ---");
            
        } catch (StructureAcademiqueException e) {
            logger.error("ERREUR dans testStructureAcademique: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("ERREUR INATTENDUE dans testStructureAcademique: {}", e.getMessage(), e);
        }
    }}

    /*private void testUtilisateurs(GestionUtilisateursMetier metier) {
        logger.info("--- TEST GESTION UTILISATEURS ---");
        
        try {
            // Créer un administrateur
            AdministrateurDTO administrateurDTO = AdministrateurDTO.builder()
                .ppr("12345678")
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@mycampus.ma")
                .fonction("Administrateur Système")
                .grade("A1")
                .build();
            
            AdministrateurDTO adminCree = metier.creerAdministrateur(administrateurDTO);
            logger.info("Administrateur créé: {} - {} {}", adminCree.getId(), adminCree.getPrenom(), adminCree.getNom());
            
            // Tester la recherche par PPR
            <AdministrateurDTO> adminParPpr = metier.trouverAdministrateurParPpr("12345678");
            if (adminParPpr.isPresent()) {
                logger.info("Administrateur trouvé par PPR: {}", adminParPpr.get().getEmail());
            }
            
            // Lister tous les administrateurs
            ListeAdministrateursDTO listeAdmins = metier.listerTousLesAdministrateurs();
            logger.info("Total administrateurs: Actifs={}, Inactifs={}", 
                listeAdmins.getTotalActifs(), listeAdmins.getTotalInactifs());
            
            // Modifier un administrateur
            adminCree.setFonction("Administrateur Principal");
            AdministrateurDTO adminModifie = metier.modifierAdministrateur(adminCree.getId(), adminCree);
            logger.info("Administrateur modifié: {}", adminModifie.getFonction());
            
            // Tester activation/désactivation
            metier.desactiverAdministrateur(adminCree.getId());
            logger.info("Administrateur désactivé");
            
            metier.activerAdministrateur(adminCree.getId());
            logger.info("Administrateur réactivé");
            
            logger.info("--- TEST GESTION UTILISATEURS TERMINÉ AVEC SUCCÈS ---");
            
        } catch (UtilisateursException e) {
            logger.error("ERREUR dans testUtilisateurs: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("ERREUR INATTENDUE dans testUtilisateurs: {}", e.getMessage(), e);
        }
    }


}


     */