package com.mycampus.backend;

import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.*;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.AffectationEnseignementModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.DepartementModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.FiliereModificationDTO;
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
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableJpaAuditing
public class BackendApplication {

    private static final Logger logger = LoggerFactory.getLogger(BackendApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    //@Bean
    CommandLineRunner commandLineRunner(
            GestionStructureAcademiqueMetier structureMetier,
            GestionUtilisateursMetier utilisateursMetier
    ) {
        return args -> {
            logger.info("=== SEED MULTI START ===");

            // 1) Structure
            createStructureBulk(structureMetier);

            // 2) Users
            boolean usersOk = createUsersBulk(utilisateursMetier);
            if (!usersOk) {
                logger.warn("[SEED] Users failed => skip affectations.");
                logger.info("=== SEED MULTI END ===");
                return;
            }

            // 3) Affectations (après users)
            List<Long> affectationIds = createAffectationsBulk(structureMetier);

            // 4) Update quelques éléments (optionnel)
            updateSome(structureMetier, utilisateursMetier, affectationIds);

            logger.info("=== SEED MULTI END ===");
        };
    }

    // ----------------------------
    // CREATE STRUCTURE (BULK)
    // ----------------------------
    private void createStructureBulk(GestionStructureAcademiqueMetier metier) {
        logger.info("[STRUCTURE] Bulk create...");

        // Exemple: 2 départements
        List<DepartementEntrerDTO> departements = List.of(
                DepartementEntrerDTO.builder()
                        .codeDepartement("INFO")
                        .typeDepartement("Informatique")
                        .description("Département Informatique")
                        .build(),
                DepartementEntrerDTO.builder()
                        .codeDepartement("INDUS")
                        .typeDepartement("Génie Industriel")
                        .description("Département Industriel")
                        .build()
        );

        // Exemple: filières liées aux départements
        List<FiliereEntrerDTO> filieres = List.of(
                FiliereEntrerDTO.builder()
                        .codeFiliere("ILCS")
                        .nom("Licence Cyber Sécurité")
                        .description("Filière cybersécurité")
                        .dureeFormation("3 Ans")
                        .niveauDiplome("Licence")
                        .etat(EtatStructure.OUVERTE)
                        .codeDepartement("INFO")
                        .build(),
                FiliereEntrerDTO.builder()
                        .codeFiliere("GL")
                        .nom("Génie Logiciel")
                        .description("Filière génie logiciel")
                        .dureeFormation("3 Ans")
                        .niveauDiplome("Licence")
                        .etat(EtatStructure.OUVERTE)
                        .codeDepartement("INFO")
                        .build(),
                FiliereEntrerDTO.builder()
                        .codeFiliere("GIND")
                        .nom("Génie Industriel")
                        .description("Filière industrielle")
                        .dureeFormation("3 Ans")
                        .niveauDiplome("Licence")
                        .etat(EtatStructure.OUVERTE)
                        .codeDepartement("INDUS")
                        .build()
        );

        // Classes par filière
        List<ClasseEntrerDTO> classes = List.of(
                ClasseEntrerDTO.builder()
                        .codeClasse("ILCS1")
                        .nom("ILCS - Groupe A")
                        .niveau("1ère année")
                        .anneeUniversitaire("2025/2026")
                        .effectifMax(30)
                        .codeFiliere("ILCS")
                        .build(),
                ClasseEntrerDTO.builder()
                        .codeClasse("ILCS2")
                        .nom("ILCS - Groupe B")
                        .niveau("1ère année")
                        .anneeUniversitaire("2025/2026")
                        .effectifMax(30)
                        .codeFiliere("ILCS")
                        .build(),
                ClasseEntrerDTO.builder()
                        .codeClasse("GL1")
                        .nom("GL - Groupe A")
                        .niveau("1ère année")
                        .anneeUniversitaire("2025/2026")
                        .effectifMax(35)
                        .codeFiliere("GL")
                        .build(),
                ClasseEntrerDTO.builder()
                        .codeClasse("GIND1")
                        .nom("GIND - Groupe A")
                        .niveau("1ère année")
                        .anneeUniversitaire("2025/2026")
                        .effectifMax(25)
                        .codeFiliere("GIND")
                        .build()
        );

        // Modules par filière
        List<ModuleEntrerDTO> modules = List.of(
                ModuleEntrerDTO.builder()
                        .codeModule("MATH101")
                        .nom("Maths 1")
                        .description("Mathématiques de base")
                        .semestre(Semestre.S1)
                        .coefficient(3)
                        .codeFiliere("ILCS")
                        .build(),
                ModuleEntrerDTO.builder()
                        .codeModule("SEC101")
                        .nom("Sécurité 1")
                        .description("Intro sécurité")
                        .semestre(Semestre.S1)
                        .coefficient(4)
                        .codeFiliere("ILCS")
                        .build(),
                ModuleEntrerDTO.builder()
                        .codeModule("JAVA101")
                        .nom("Java 1")
                        .description("Programmation Java")
                        .semestre(Semestre.S1)
                        .coefficient(4)
                        .codeFiliere("GL")
                        .build()
        );

        // Matières par module
        List<MatiereEntrerDTO> matieres = List.of(
                MatiereEntrerDTO.builder()
                        .codeMatiere("ALGEBRE")
                        .nom("Algèbre")
                        .description("Algèbre linéaire")
                         .codeModule("MATH101")
                        .build(),
                MatiereEntrerDTO.builder()
                        .codeMatiere("ANALYSE")
                        .nom("Analyse")
                        .description("Analyse 1")
                         .codeModule("MATH101")
                        .build(),
                MatiereEntrerDTO.builder()
                        .codeMatiere("CRYPTO")
                        .nom("Cryptographie")
                        .description("Bases crypto")
                         .codeModule("SEC101")
                        .build(),
                MatiereEntrerDTO.builder()
                        .codeMatiere("JAVA_BASICS")
                        .nom("Java Basics")
                        .description("OOP + syntaxe")
                         .codeModule("JAVA101")
                        .build()
        );

        try {
            for (DepartementEntrerDTO d : departements) {
                DepartementSortieDTO out = metier.creerDepartement(d);
                logger.info("[STRUCTURE] Département OK: {}", out.getCodeDepartement());
            }
            for (FiliereEntrerDTO f : filieres) {
                FiliereSortieDTO out = metier.creerFiliere(f);
                logger.info("[STRUCTURE] Filière OK: {}", out.getCodeFiliere());
            }
            for (ClasseEntrerDTO c : classes) {
                ClasseSortieDTO out = metier.creerClasse(c);
                logger.info("[STRUCTURE] Classe OK: {}", out.getCodeClasse());
            }
            for (ModuleEntrerDTO m : modules) {
                ModuleSortieDTO out = metier.creerModule(m);
                logger.info("[STRUCTURE] Module OK: {}", out.getCodeModule());
            }
            for (MatiereEntrerDTO m : matieres) {
                MatiereSortieDTO out = metier.creerMatiere(m);
                logger.info("[STRUCTURE] Matière OK: {}", out.getCodeMatiere());
            }

        } catch (StructureAcademiqueException e) {
            logger.error("[STRUCTURE] Error: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[STRUCTURE] Unexpected error", e);
        }
    }

    // ----------------------------
    // CREATE USERS (BULK)
    // ----------------------------
    private boolean createUsersBulk(GestionUtilisateursMetier metier) {
        logger.info("[USERS] Bulk create...");

        try {
            // 2 admins
            List<AdministrateurEntrerDTO> admins = List.of(
                    AdministrateurEntrerDTO.builder()
                            .ppr("10000001")
                            .nom("Admin")
                            .prenom("One")
                            .email("admin1@mycampus.ma")
                            .cin("AD111111")
                            .password("password123")
                            .tel("0611111111")
                            .grade("PH")     // IMPORTANT: not null
                            .fonction("Admin") // mets une valeur si ta colonne est not null
                            .build(),
                    AdministrateurEntrerDTO.builder()
                            .ppr("10000002")
                            .nom("Admin")
                            .prenom("Two")
                            .email("admin2@mycampus.ma")
                            .cin("AD222222")
                            .password("password123")
                            .tel("0622222222")
                            .grade("PES")
                            .fonction("Admin")
                            .build()
            );

            // Professeurs (liés à departementOuTravail par code)
            List<ProfesseurEntrerDTO> profs = List.of(
                    ProfesseurEntrerDTO.builder()
                            .ppr("20000001")
                            .nom("Martin")
                            .prenom("Pierre")
                            .email("prof1@mycampus.ma")
                            .cin("PR111111")
                            .password("password123")
                            .tel("0633333333")
                            .specialite("Cybersécurité")
                            .grade("PES")
                            .statut("Titulaire")
                            .codeDepartementOuTravail("INFO")
                            .dateRecrutement(LocalDate.of(2020, 9, 1))
                            .build(),
                    ProfesseurEntrerDTO.builder()
                            .ppr("20000002")
                            .nom("Alaoui")
                            .prenom("Sara")
                            .email("prof2@mycampus.ma")
                            .cin("PR222222")
                            .password("password123")
                            .tel("0644444444")
                            .specialite("Mathématiques")
                            .grade("PH")
                            .statut("Vacataire")
                            .codeDepartementOuTravail("INFO")
                            .dateRecrutement(LocalDate.of(2021, 9, 1))
                            .build(),
                    ProfesseurEntrerDTO.builder()
                            .ppr("20000003")
                            .nom("Hassan")
                            .prenom("Omar")
                            .email("prof3@mycampus.ma")
                            .cin("PR333333")
                            .password("password123")
                            .tel("0655555555")
                            .specialite("Génie industriel")
                            .grade("PES")
                            .statut("Titulaire")
                            .codeDepartementOuTravail("INDUS")
                            .dateRecrutement(LocalDate.of(2019, 9, 1))
                            .build()
            );

            // Etudiants (liés à classe par codeClasse)
            List<EtudiantEntrerDTO> etudiants = List.of(
                    EtudiantEntrerDTO.builder()
                            .apogee("30000001")
                            .nom("Bernard")
                            .prenom("Sophie")
                            .email("etud1@mycampus.ma")
                            .cin("ET111111")
                            .password("password123")
                            .tel("0666666666")
                            .niveau("1ère année")
                            .anneeInscription(2025)
                            .semestreActuel(Semestre.S1)
                            .codeClasse("ILCS1")
                            .dateNaissance(LocalDate.of(2002, 5, 15))
                            .etatInscription(EtatInscription.INSCRIT)
                            .build(),
                    EtudiantEntrerDTO.builder()
                            .apogee("30000002")
                            .nom("Zahra")
                            .prenom("Imane")
                            .email("etud2@mycampus.ma")
                            .cin("ET222222")
                            .password("password123")
                            .tel("0677777777")
                            .niveau("1ère année")
                            .anneeInscription(2025)
                            .semestreActuel(Semestre.S1)
                            .codeClasse("ILCS2")
                            .dateNaissance(LocalDate.of(2003, 2, 10))
                            .etatInscription(EtatInscription.INSCRIT)
                            .build(),
                    EtudiantEntrerDTO.builder()
                            .apogee("30000003")
                            .nom("Karim")
                            .prenom("Youssef")
                            .email("etud3@mycampus.ma")
                            .cin("ET333333")
                            .password("password123")
                            .tel("0688888888")
                            .niveau("1ère année")
                            .anneeInscription(2025)
                            .semestreActuel(Semestre.S1)
                            .codeClasse("GL1")
                            .dateNaissance(LocalDate.of(2002, 11, 3))
                            .etatInscription(EtatInscription.INSCRIT)
                            .build()
            );

            for (AdministrateurEntrerDTO a : admins) {
                AdministrateurSortieDTO out = metier.creerAdministrateur(a);
                logger.info("[USERS] Admin OK: {}", out.getPpr());
            }
            for (ProfesseurEntrerDTO p : profs) {
                ProfesseurSortieDTO out = metier.creerProfesseur(p);
                logger.info("[USERS] Prof OK: {}", out.getPpr());
            }
            for (EtudiantEntrerDTO e : etudiants) {
                EtudiantSortieDTO out = metier.creerEtudiant(e);
                logger.info("[USERS] Etudiant OK: {}", out.getApogee());
            }

            return true;

        } catch (UtilisateursException e) {
            logger.error("[USERS] Error: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("[USERS] Unexpected error", e);
            return false;
        }
    }

    // ----------------------------
    // CREATE AFFECTATIONS (BULK)
    // ----------------------------
    private List<Long> createAffectationsBulk(GestionStructureAcademiqueMetier metier) {
        logger.info("[AFFECTATION] Bulk create...");
        List<Long> ids = new ArrayList<>();

        // Exemple: plusieurs affectations
        List<AffectationEnseignementEntrerDTO> affectations = List.of(
                AffectationEnseignementEntrerDTO.builder()

                        .professeurPpr("20000002") // prof maths
                        .codeClasse("ILCS1")
                        .codeMatiere("ALGEBRE")
                        .active(true)
                        .build(),
                AffectationEnseignementEntrerDTO.builder()

                        .professeurPpr("20000001") // prof cyber
                        .codeClasse("ILCS1")
                        .codeMatiere("CRYPTO")
                        .active(true)
                        .build(),
                AffectationEnseignementEntrerDTO.builder()

                        .professeurPpr("20000001")
                        .codeClasse("ILCS2")
                        .codeMatiere("CRYPTO")
                        .active(true)
                        .build()
        );

        for (AffectationEnseignementEntrerDTO a : affectations) {
            try {
                AffectationEnseignementSortieDTO out = metier.creerAffectationEnseignement(a);
                ids.add(out.getId());
                logger.info("[AFFECTATION] OK id={} prof={} classe={} matiere={}",
                        out.getId(), out.getProfesseurPpr(), out.getCodeClasse(), out.getCodeMatiere());
            } catch (StructureAcademiqueException e) {
                logger.error("[AFFECTATION] Error: {}", e.getMessage());
            } catch (Exception e) {
                logger.error("[AFFECTATION] Unexpected error", e);
            }
        }
        return ids;
    }

    // ----------------------------
    // UPDATE (EXEMPLES)
    // ----------------------------
    private void updateSome(
            GestionStructureAcademiqueMetier structureMetier,
            GestionUtilisateursMetier utilisateursMetier,
            List<Long> affectationIds
    ) {
        logger.info("[UPDATE] Examples...");

        try {
            // Update departement INFO
            DepartementModificationDTO depUp = DepartementModificationDTO.builder()
                     .typeDepartement("Informatique & Réseaux")
                    .description("Département INFO mis à jour")
                    .chefDepartementPpr("20000001")
                    .build();
            structureMetier.modifierDepartement("INFO", depUp);

            DepartementModificationDTO depup1 = DepartementModificationDTO.builder()
                     .typeDepartement("Génie Industriel")
                    .description("Département Industriel up")
                   .chefDepartementPpr("20000002")
                    .build();
            structureMetier.modifierDepartement("INDUS", depup1);

            FiliereModificationDTO fil1 = FiliereModificationDTO.builder()

                    .nom("Génie Logiciel")
                    .description("Filière génie logiciel up")
                    .dureeFormation("3 Ans")
                    .niveauDiplome("Licence")
                    .etat(EtatStructure.OUVERTE)
                    .codeDepartement("INFO")
                    .chefFilierePpr("20000003")
                    .build();
            structureMetier.modifierFiliere("GL", fil1);

            FiliereModificationDTO fil2 =     FiliereModificationDTO.builder()
                             .nom("Génie Industriel")
                            .description("Filière industrielle")
                            .dureeFormation("3 Ans")
                            .niveauDiplome("Licence")
                            .etat(EtatStructure.OUVERTE)
                            .codeDepartement("INDUS")
                       .chefFilierePpr("20000002")
                            .build();
            structureMetier.modifierFiliere("GIND", fil2);


            // Update filiere ILCS
            FiliereModificationDTO filUp = FiliereModificationDTO.builder()
                     .nom("ILCS - Cyber Sécurité Avancée")
                    .description("Filière ILCS update")
                    .dureeFormation("3 Ans")
                    .niveauDiplome("Licence")
                    .etat(EtatStructure.OUVERTE)
                    .codeDepartement("INFO")
                    .chefFilierePpr("20000001")
                    .build();
            structureMetier.modifierFiliere("ILCS", filUp);

            logger.info("[UPDATE] Structure update OK.");

        } catch (StructureAcademiqueException e) {
            logger.error("[UPDATE] Structure error: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[UPDATE] Structure unexpected", e);
        }

        // Affectation update exemple (si ta méthode existe: modifierAffectationEnseignement(id, dto))
        if (affectationIds != null && !affectationIds.isEmpty()) {
            Long firstId = affectationIds.get(0);
            try {
                AffectationEnseignementModificationDTO affUp = AffectationEnseignementModificationDTO.builder()
                        .professeurPpr("20000002")
                        .codeClasse("ILCS1")
                        .codeMatiere("ALGEBRE")
                        .active(true)
                        .build();

                // Décommente uniquement si ta signature est bien (Long id, DTO)
                structureMetier.modifierAffectationEnseignement(firstId, affUp);

                logger.info("[UPDATE] Affectation update OK id={}", firstId);
            } catch (StructureAcademiqueException e) {
                logger.error("[UPDATE] Affectation error: {}", e.getMessage());
            } catch (Exception e) {
                logger.error("[UPDATE] Affectation unexpected", e);
            }
        }
    }
}
