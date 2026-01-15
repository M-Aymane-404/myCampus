package com.mycampus.backend;

import com.mycampus.backend.dto.administrateur.DonneEntrer.*;
import com.mycampus.backend.dto.administrateur.DonneSortie.*;
import com.mycampus.backend.enumerations.EtatInscription;
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
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
@EnableJpaAuditing
public class BackendApplication {

    private static final Logger logger = LoggerFactory.getLogger(BackendApplication.class);

    // Codes fixes pour les tests
    private static final String CODE_DEP = "INFO";
    private static final String CODE_FIL = "ILCS";
    private static final String CODE_CLS = "ILCS1";
    private static final String CODE_MOD = "MATH101";
    private static final String CODE_MAT = "ALGEBRE";

    private static final String ADMIN_PPR = "12345678";
    private static final String PROF_PPR  = "87654321";
    private static final String ETUD_APOG = "202300123";

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(
            GestionStructureAcademiqueMetier structureMetier,
            GestionUtilisateursMetier utilisateursMetier
    ) {
        return args -> {
            logger.info("=== DÉMARRAGE DES TESTS (CODES MÉTIERS) ===");

            createStructure(structureMetier);
            createUsers(utilisateursMetier);
            Long affectationId = createAffectation(structureMetier);

            readAll(structureMetier, utilisateursMetier);
            updateAll(structureMetier, utilisateursMetier, affectationId);

            activateDeactivateAll(utilisateursMetier, structureMetier, affectationId);

            logger.info("=== FIN DES TESTS (CODES MÉTIERS) ===");
        };
    }

    // ----------------------------
    // CREATE
    // ----------------------------
    private void createStructure(GestionStructureAcademiqueMetier metier) {
        logger.info("[STRUCTURE] Création structure académique...");

        try {
            // Département
            DepartementEntrerDTO depIn = DepartementEntrerDTO.builder()
                    .codeDepartement(CODE_DEP)
                    .typeDepartement("Informatique")
                    .description("Département d'informatique")
                    .build();
            DepartementSortieDTO depOut = metier.creerDepartement(depIn);
            logger.info("[STRUCTURE] Département OK: {}", depOut.getCodeDepartement());

            // Filière
            FiliereEntrerDTO filIn = FiliereEntrerDTO.builder()
                    .codeFiliere(CODE_FIL)
                    .nom("Informatique Licence Cyber Sécurité")
                    .description("Formation en cybersécurité")
                    .dureeFormation("3 Ans")
                    .niveauDiplome("Licence")
                    .etat(EtatStructure.OUVERTE)
                    .codeDepartement(CODE_DEP)
                    .build();
            FiliereSortieDTO filOut = metier.creerFiliere(filIn);
            logger.info("[STRUCTURE] Filière OK: {}", filOut.getCodeFiliere());

            // Classe
            ClasseEntrerDTO clsIn = ClasseEntrerDTO.builder()
                    .codeClasse(CODE_CLS)
                    .nom("ILCS Semestre 1")
                    .niveau("1ère année")
                    .anneeUniversitaire("2025/2026")
                    .effectifMax(30)
                    .codeFiliere(CODE_FIL)
                    .build();
            ClasseSortieDTO clsOut = metier.creerClasse(clsIn);
            logger.info("[STRUCTURE] Classe OK: {}", clsOut.getCodeClasse());

            // Module
            ModuleEntrerDTO modIn = ModuleEntrerDTO.builder()
                    .codeModule(CODE_MOD)
                    .nom("Mathématiques Fondamentales")
                    .description("Module de mathématiques de base")
                    .semestre(Semestre.S1)
                    .coefficient(3)
                    .codeFiliere(CODE_FIL)
                    .build();
            ModuleSortieDTO modOut = metier.creerModule(modIn);
            logger.info("[STRUCTURE] Module OK: {}", modOut.getCodeModule());

            // Matière
            MatiereEntrerDTO matIn = MatiereEntrerDTO.builder()
                    .codeMatiere(CODE_MAT)
                    .nom("Algèbre Linéaire")
                    .description("Cours d'algèbre linéaire")
                    .coefficient(2)
                    .codeModule(CODE_MOD)
                    .build();
            MatiereSortieDTO matOut = metier.creerMatiere(matIn);
            logger.info("[STRUCTURE] Matière OK: {}", matOut.getCodeMatiere());

        } catch (StructureAcademiqueException e) {
            logger.error("[STRUCTURE] Erreur création structure: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[STRUCTURE] Erreur inattendue création structure", e);
        }
    }

    private void createUsers(GestionUtilisateursMetier metier) {
        logger.info("[UTILISATEURS] Création utilisateurs...");

        try {
            // Admin
            AdministrateurEntrerDTO adminIn = AdministrateurEntrerDTO.builder()
                    .ppr(ADMIN_PPR)
                    .nom("Dupont")
                    .prenom("Jean")
                    .email("jean.dupont@mycampus.ma")
                    .cin("AA123456")
                    .password("password123")
                    .tel("0612345678")
                    .grade("Fes")
                    .build();
            AdministrateurSortieDTO adminOut = metier.creerAdministrateur(adminIn);
            logger.info("[UTILISATEURS] Admin OK: {}", adminOut.getPpr());

            // Prof
            ProfesseurEntrerDTO profIn = ProfesseurEntrerDTO.builder()
                    .ppr(PROF_PPR)
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
            ProfesseurSortieDTO profOut = metier.creerProfesseur(profIn);
            logger.info("[UTILISATEURS] Prof OK: {}", profOut.getPpr());

            // Étudiant (lié par codeClasse)
            EtudiantEntrerDTO etudIn = EtudiantEntrerDTO.builder()
                    .apogee(ETUD_APOG)
                    .nom("Bernard")
                    .prenom("Sophie")
                    .email("sophie.bernard@mycampus.ma")
                    .cin("CC123789")
                    .password("password789")
                    .tel("0634567890")
                    .niveau("1ère année")
                    .anneeInscription(2025)
                    .semestreActuel(Semestre.S1)
                    .codeClasse(CODE_CLS)
                    .dateNaissance(LocalDate.of(2002, 5, 15))
                    .etatInscription(EtatInscription.INSCRIT)
                    .build();
            EtudiantSortieDTO etudOut = metier.creerEtudiant(etudIn);
            logger.info("[UTILISATEURS] Étudiant OK: {}", etudOut.getApogee());

        } catch (UtilisateursException e) {
            logger.error("[UTILISATEURS] Erreur création utilisateurs: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[UTILISATEURS] Erreur inattendue création utilisateurs", e);
        }
    }

    private Long createAffectation(GestionStructureAcademiqueMetier metier) {
        logger.info("[AFFECTATION] Création affectation...");

        try {
            AffectationEnseignementEntrerDTO affIn = AffectationEnseignementEntrerDTO.builder()
                    .anneeUniversitaire("2025/2026")
                    .semestre(Semestre.S1)
                    .professeurPpr(PROF_PPR)
                    .codeClasse(CODE_CLS)
                    .codeMatiere(CODE_MAT)
                    .build();

            AffectationEnseignementSortieDTO affOut = metier.creerAffectationEnseignement(affIn);
            logger.info("[AFFECTATION] Affectation OK: id={}, prof={}, classe={}, matiere={}",
                    affOut.getId(), affOut.getProfesseurPpr(), affOut.getCodeClasse(), affOut.getCodeMatiere());

            return affOut.getId();

        } catch (StructureAcademiqueException e) {
            logger.error("[AFFECTATION] Erreur création affectation: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("[AFFECTATION] Erreur inattendue création affectation", e);
            return null;
        }
    }

    // ----------------------------
    // READ
    // ----------------------------
    private void readAll(GestionStructureAcademiqueMetier structureMetier, GestionUtilisateursMetier utilisateursMetier) {
        logger.info("[READ] Lecture / listing...");

        try {
            List<DepartementSortieDTO> deps = structureMetier.listerTousLesDepartements();
            List<FiliereSortieDTO> fils = structureMetier.listerToutesLesFilieres();
            List<ClasseSortieDTO> clss = structureMetier.listerToutesLesClasses();
            List<ModuleSortieDTO> mods = structureMetier.listerTousLesModules();
            List<MatiereSortieDTO> mats = structureMetier.listerToutesLesMatieres();
            List<AffectationEnseignementSortieDTO> affs = structureMetier.listerToutesLesAffectations();

            logger.info("[READ] Departements={}, Filieres={}, Classes={}, Modules={}, Matieres={}, Affectations={}",
                    deps.size(), fils.size(), clss.size(), mods.size(), mats.size(), affs.size());

            structureMetier.trouverDepartementParCode(CODE_DEP);
            structureMetier.trouverFiliereParCode(CODE_FIL);
            structureMetier.trouverClasseParCode(CODE_CLS);
            structureMetier.trouverModuleParCode(CODE_MOD);
            structureMetier.trouverMatiereParCode(CODE_MAT);

        } catch (StructureAcademiqueException e) {
            logger.error("[READ] Erreur structure: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[READ] Erreur inattendue structure", e);
        }

        try {
            utilisateursMetier.trouverAdministrateurParPpr(ADMIN_PPR);
            utilisateursMetier.trouverProfesseurParPpr(PROF_PPR);
            utilisateursMetier.trouverEtudiantParApogee(ETUD_APOG);

            logger.info("[READ] Utilisateurs trouvés par identifiants métiers (OK).");

        } catch (UtilisateursException e) {
            logger.error("[READ] Erreur utilisateurs: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[READ] Erreur inattendue utilisateurs", e);
        }
    }

    // ----------------------------
    // UPDATE
    // ----------------------------
    private void updateAll(
            GestionStructureAcademiqueMetier structureMetier,
            GestionUtilisateursMetier utilisateursMetier,
            Long affectationId
    ) {
        logger.info("[UPDATE] Mise à jour...");

        try {
            DepartementEntrerDTO depUp = DepartementEntrerDTO.builder()
                    .codeDepartement(CODE_DEP)
                    .typeDepartement("Informatique et Réseaux")
                    .description("Département mis à jour")
                    .build();
            structureMetier.modifierDepartement(CODE_DEP, depUp);

            FiliereEntrerDTO filUp = FiliereEntrerDTO.builder()
                    .codeFiliere(CODE_FIL)
                    .nom("ILCS - Cybersécurité Spécialisée")
                    .description("Filière mise à jour")
                    .dureeFormation("3 Ans")
                    .niveauDiplome("Licence")
                    .etat(EtatStructure.OUVERTE)
                    .codeDepartement(CODE_DEP)
                    .build();
            structureMetier.modifierFiliere(CODE_FIL, filUp);

            ClasseEntrerDTO clsUp = ClasseEntrerDTO.builder()
                    .codeClasse(CODE_CLS)
                    .nom("ILCS1 - Groupe A")
                    .niveau("1ère année")
                    .anneeUniversitaire("2025/2026")
                    .effectifMax(35)
                    .codeFiliere(CODE_FIL)
                    .build();
            structureMetier.modifierClasse(CODE_CLS, clsUp);

            ModuleEntrerDTO modUp = ModuleEntrerDTO.builder()
                    .codeModule(CODE_MOD)
                    .nom("Maths Appliquées")
                    .description("Module mis à jour")
                    .semestre(Semestre.S1)
                    .coefficient(4)
                    .codeFiliere(CODE_FIL)
                    .build();
            structureMetier.modifierModule(CODE_MOD, modUp);

            MatiereEntrerDTO matUp = MatiereEntrerDTO.builder()
                    .codeMatiere(CODE_MAT)
                    .nom("Algèbre Avancée")
                    .description("Matière mise à jour")
                    .coefficient(3)
                    .codeModule(CODE_MOD)
                    .build();
            structureMetier.modifierMatiere(CODE_MAT, matUp);

            logger.info("[UPDATE] Structure mise à jour (OK).");

        } catch (StructureAcademiqueException e) {
            logger.error("[UPDATE] Erreur update structure: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[UPDATE] Erreur inattendue update structure", e);
        }

        try {
            AdministrateurEntrerDTO adminUp = AdministrateurEntrerDTO.builder()
                    .ppr(ADMIN_PPR)
                    .prenom("Jean Michel")
                    .email("jeanmichel.dupont@mycampus.ma")
                    .cin("AA123456")
                    .tel("0612345678")
                    .build();
            utilisateursMetier.modifierAdministrateur(ADMIN_PPR, adminUp);

            ProfesseurEntrerDTO profUp = ProfesseurEntrerDTO.builder()
                    .ppr(PROF_PPR)
                    .prenom("Pierre Alain")
                    .email("pierre.alain.martin@mycampus.ma")
                    .cin("BB987654")
                    .specialite("Cybersécurité")
                    .grade("PES")
                    .statut("Titulaire")
                    .dateRecrutement(LocalDate.of(2020, 9, 1))
                    .build();
            utilisateursMetier.modifierProfesseur(PROF_PPR, profUp);

            EtudiantEntrerDTO etudUp = EtudiantEntrerDTO.builder()
                    .apogee(ETUD_APOG)
                    .prenom("Sophie Marie")
                    .email("sophie.marie.bernard@mycampus.ma")
                    .cin("CC123789")
                    .niveau("1ère année")
                    .anneeInscription(2025)
                    .semestreActuel(Semestre.S1)
                    .codeClasse(CODE_CLS)
                    .etatInscription(EtatInscription.INSCRIT)
                    .build();
            utilisateursMetier.modifierEtudiant(ETUD_APOG, etudUp);

            logger.info("[UPDATE] Utilisateurs mis à jour (OK).");

        } catch (UtilisateursException e) {
            logger.error("[UPDATE] Erreur update utilisateurs: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[UPDATE] Erreur inattendue update utilisateurs", e);
        }

        if (affectationId != null) {
            try {
                AffectationEnseignementEntrerDTO affUp = AffectationEnseignementEntrerDTO.builder()
                        .anneeUniversitaire("2025/2026")
                        .semestre(Semestre.S2)
                        .professeurPpr(PROF_PPR)
                        .codeClasse(CODE_CLS)
                        .codeMatiere(CODE_MAT)
                        .active(true)
                        .build();
                structureMetier.modifierAffectationEnseignement(affectationId, affUp);
                logger.info("[UPDATE] Affectation mise à jour (OK) id={}", affectationId);

            } catch (StructureAcademiqueException e) {
                logger.error("[UPDATE] Erreur update affectation: {}", e.getMessage());
            } catch (Exception e) {
                logger.error("[UPDATE] Erreur inattendue update affectation", e);
            }
        }
    }

    // ----------------------------
    // ACTIVATE / DEACTIVATE
    // ----------------------------
    private void activateDeactivateAll(
            GestionUtilisateursMetier utilisateursMetier,
            GestionStructureAcademiqueMetier structureMetier,
            Long affectationId
    ) {
        logger.info("[ACTIVATION] Activation / désactivation...");

        try {
            utilisateursMetier.desactiverAdministrateur(ADMIN_PPR);
            utilisateursMetier.activerAdministrateur(ADMIN_PPR);

            utilisateursMetier.desactiverProfesseur(PROF_PPR);
            utilisateursMetier.activerProfesseur(PROF_PPR);

            utilisateursMetier.desactiverEtudiant(ETUD_APOG);
            utilisateursMetier.activerEtudiant(ETUD_APOG);

            logger.info("[ACTIVATION] Utilisateurs OK.");

        } catch (UtilisateursException e) {
            logger.error("[ACTIVATION] Erreur activation utilisateurs: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[ACTIVATION] Erreur inattendue activation utilisateurs", e);
        }

        if (affectationId != null) {
            try {
                structureMetier.desactiverAffectationEnseignement(affectationId);
                structureMetier.activerAffectationEnseignement(affectationId);
                logger.info("[ACTIVATION] Affectation OK id={}", affectationId);

            } catch (StructureAcademiqueException e) {
                logger.error("[ACTIVATION] Erreur activation affectation: {}", e.getMessage());
            } catch (Exception e) {
                logger.error("[ACTIVATION] Erreur inattendue activation affectation", e);
            }
        }
    }
}
