package com.mycampus.backend.config;

import com.mycampus.backend.entites.*;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.repositorys.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class StructureSeeder {

    private final DepartementRepository departementRepository;
    private final FiliereRepository filiereRepository;
    private final ClasseRepository classeRepository;
    private final ModuleRepository moduleRepository;
    private final MatiereRepository matiereRepository;
    private final UtilisateurRepository utilisateurRepository;

  /*  @Bean
    public ApplicationRunner structureSeederRunner() {
        return args -> {
            log.info("Starting structure data seeding...");
            seedStructure();
            log.info("Structure data seeding completed successfully");
        };
    }

    @Transactional
    public void seedStructure() {
        seedFilieres();
        seedClasses();
        seedModules();
        seedMatieres();
    }

    private void seedFilieres() {
        List<Filiere> filieres = Arrays.asList(
            Filiere.builder()
                    .codeFiliere("ILCS").nom("Ingénierie Logicielle et Conception de Systèmes")
                .description("Filière ILCS").(LocalDateTime.now())
                .build(),
            Filiere.builder()
                .code("GL").nom("Génie Logiciel").description("Filière Génie Logiciel")
                .dateCreation(LocalDateTime.now()).build(),
            Filiere.builder()
                .code("GIND").nom("Génie Industriel").description("Filière Génie Industriel")
                .dateCreation(LocalDateTime.now()).build(),
            Filiere.builder()
                .code("GEII").nom("Génie Électrique et Informatique Industrielle")
                .description("Filière GEII").dateCreation(LocalDateTime.now())
                .build(),
            Filiere.builder()
                .code("ISI").nom("Informatique et Systèmes d'Information")
                .description("Filière ISI").dateCreation(LocalDateTime.now())
                .build()
        );
        
        for (Filiere filiere : filieres) {
            if (!filiereRepository.existsByCode(filiere.getCode())) {
                filiereRepository.save(filiere);
                log.info("Created filiere: {}", filiere.getCode());
            }
        }
        
        // Set filiere chefs
        setFiliereChefs();
    }

    private void seedClasses() {
        Filiere ilcs = filiereRepository.findByCode("ILCS").orElse(null);
        Filiere gl = filiereRepository.findByCode("GL").orElse(null);
        Filiere gind = filiereRepository.findByCode("GIND").orElse(null);
        Filiere geii = filiereRepository.findByCode("GEII").orElse(null);
        Filiere isi = filiereRepository.findByCode("ISI").orElse(null);
        
        List<Classe> classes = Arrays.asList(
            Classe.builder().nom("ILCS1-A").filiere(ilcs).niveau(1).dateCreation(LocalDateTime.now()).build(),
            Classe.builder().nom("ILCS1-B").filiere(ilcs).niveau(1).dateCreation(LocalDateTime.now()).build(),
            Classe.builder().nom("GL1-A").filiere(gl).niveau(1).dateCreation(LocalDateTime.now()).build(),
            Classe.builder().nom("GL2-A").filiere(gl).niveau(2).dateCreation(LocalDateTime.now()).build(),
            Classe.builder().nom("GIND1-A").filiere(gind).niveau(1).dateCreation(LocalDateTime.now()).build(),
            Classe.builder().nom("GIND2-A").filiere(gind).niveau(2).dateCreation(LocalDateTime.now()).build(),
            Classe.builder().nom("GEII2-A").filiere(geii).niveau(2).dateCreation(LocalDateTime.now()).build(),
            Classe.builder().nom("ISI2-A").filiere(isi).niveau(2).dateCreation(LocalDateTime.now()).build(),
            Classe.builder().nom("ISI2-B").filiere(isi).niveau(2).dateCreation(LocalDateTime.now()).build()
        );
        
        for (Classe classe : classes) {
            if (!classeRepository.existsByNom(classe.getNom())) {
                classeRepository.save(classe);
                log.info("Created class: {}", classe.getNom());
            }
        }
    }

    private void seedModules() {
        Filiere ilcs = filiereRepository.findByCode("ILCS").orElse(null);
        Filiere gl = filiereRepository.findByCode("GL").orElse(null);
        Filiere gind = filiereRepository.findByCode("GIND").orElse(null);
        Filiere geii = filiereRepository.findByCode("GEII").orElse(null);
        Filiere isi = filiereRepository.findByCode("ISI").orElse(null);
        
        List<Module> modules = Arrays.asList(
            Module.builder().code("ILCS-MATH-S1").nom("Mathématiques S1").filiere(ilcs).semestre(Semestre.S1).dateCreation(LocalDateTime.now()).build(),
            Module.builder().code("ILCS-SEC-S1").nom("Sécurité S1").filiere(ilcs).semestre(Semestre.S1).dateCreation(LocalDateTime.now()).build(),
            Module.builder().code("ILCS-ALGO-S1").nom("Algorithmique S1").filiere(ilcs).semestre(Semestre.S1).dateCreation(LocalDateTime.now()).build(),
            Module.builder().code("GL-DEV-S1").nom("Développement S1").filiere(gl).semestre(Semestre.S1).dateCreation(LocalDateTime.now()).build(),
            Module.builder().code("GL-TEST-S1").nom("Testing S1").filiere(gl).semestre(Semestre.S1).dateCreation(LocalDateTime.now()).build(),
            Module.builder().code("GIND-PROD-S1").nom("Production S1").filiere(gind).semestre(Semestre.S1).dateCreation(LocalDateTime.now()).build(),
            Module.builder().code("GIND-QUAL-S1").nom("Qualité S1").filiere(gind).semestre(Semestre.S1).dateCreation(LocalDateTime.now()).build(),
            Module.builder().code("GEII-ELEC-S3").nom("Électronique S3").filiere(geii).semestre(Semestre.S3).dateCreation(LocalDateTime.now()).build(),
            Module.builder().code("GEII-AUTO-S3").nom("Automatisme S3").filiere(geii).semestre(Semestre.S3).dateCreation(LocalDateTime.now()).build(),
            Module.builder().code("ISI-WEB-S3").nom("Web S3").filiere(isi).semestre(Semestre.S3).dateCreation(LocalDateTime.now()).build(),
            Module.builder().code("ISI-BDD-S3").nom("Bases de Données S3").filiere(isi).semestre(Semestre.S3).dateCreation(LocalDateTime.now()).build(),
            Module.builder().code("ISI-RESEAU-S3").nom("Réseaux S3").filiere(isi).semestre(Semestre.S3).dateCreation(LocalDateTime.now()).build()
        );
        
        for (Module module : modules) {
            if (!moduleRepository.existsByCode(module.getCode())) {
                moduleRepository.save(module);
                log.info("Created module: {}", module.getCode());
            }
        }
    }

    private void seedMatieres() {
        Module ilcsMath = moduleRepository.findByCode("ILCS-MATH-S1").orElse(null);
        Module ilcsSec = moduleRepository.findByCode("ILCS-SEC-S1").orElse(null);
        Module ilcsAlgo = moduleRepository.findByCode("ILCS-ALGO-S1").orElse(null);
        Module glDev = moduleRepository.findByCode("GL-DEV-S1").orElse(null);
        Module glTest = moduleRepository.findByCode("GL-TEST-S1").orElse(null);
        Module gindProd = moduleRepository.findByCode("GIND-PROD-S1").orElse(null);
        Module gindQual = moduleRepository.findByCode("GIND-QUAL-S1").orElse(null);
        Module geiiElec = moduleRepository.findByCode("GEII-ELEC-S3").orElse(null);
        Module geiiAuto = moduleRepository.findByCode("GEII-AUTO-S3").orElse(null);
        Module isiWeb = moduleRepository.findByCode("ISI-WEB-S3").orElse(null);
        Module isiBdd = moduleRepository.findByCode("ISI-BDD-S3").orElse(null);
        Module isiReseau = moduleRepository.findByCode("ISI-RESEAU-S3").orElse(null);
        
        List<Matiere> matieres = Arrays.asList(
            Matiere.builder().id(30L).code("ALGEBRE").nom("Algèbre Linéaire").module(ilcsMath).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(31L).code("ANALYSE").nom("Analyse").module(ilcsMath).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(32L).code("CRYPTO").nom("Cryptographie").module(ilcsSec).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(33L).code("RESEAU_SEC").nom("Sécurité Réseau").module(ilcsSec).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(34L).code("STRUCTURES").nom("Structures de Données").module(ilcsAlgo).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(35L).code("COMPLEXITE").nom("Complexité").module(ilcsAlgo).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(36L).code("JAVA").nom("Programmation Java").module(glDev).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(37L).code("UML").nom("UML").module(glDev).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(38L).code("UNIT_TEST").nom("Testing Unitaire").module(glTest).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(39L).code("INT_TEST").nom("Testing Intégration").module(glTest).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(40L).code("LEAN").nom("Lean Manufacturing").module(gindProd).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(41L).code("SIX_SIGMA").nom("Six Sigma").module(gindProd).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(42L).code("ISO_9001").nom("ISO 9001").module(gindQual).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(43L).code("AUDIT_QUAL").nom("Audit Qualité").module(gindQual).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(44L).code("CIRCUITS").nom("Circuits Électriques").module(geiiElec).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(45L).code("ELECTRONIQUE").nom("Électronique").module(geiiElec).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(46L).code("AUTOMATE").nom("Automates").module(geiiAuto).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(47L).code("PLC").nom("PLC").module(geiiAuto).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(48L).code("HTML_CSS").nom("HTML/CSS").module(isiWeb).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(49L).code("JAVASCRIPT").nom("JavaScript").module(isiWeb).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(50L).code("SQL").nom("SQL").module(isiBdd).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(51L).code("NOSQL").nom("NoSQL").module(isiBdd).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(52L).code("TCP_IP").nom("TCP/IP").module(isiReseau).dateCreation(LocalDateTime.now()).build(),
            Matiere.builder().id(53L).code("SEC_RESEAU").nom("Sécurité Réseau").module(isiReseau).dateCreation(LocalDateTime.now()).build()
        );
        
        for (Matiere matiere : matieres) {
            if (!matiereRepository.existsByCode(matiere.getCode())) {
                matiereRepository.save(matiere);
                log.info("Created matiere: {}", matiere.getCode());
            }
        }
    }

    private void setFiliereChefs() {
        Filiere ilcs = filiereRepository.findByCode("ILCS").orElse(null);
        Filiere gl = filiereRepository.findByCode("GL").orElse(null);
        Filiere gind = filiereRepository.findByCode("GIND").orElse(null);
        Filiere geii = filiereRepository.findByCode("GEII").orElse(null);
        Filiere isi = filiereRepository.findByCode("ISI").orElse(null);
        
        if (ilcs != null && ilcs.getChefFiliere() == null) {
            Professeur chefIlcs = (Professeur) utilisateurRepository.findByEmail("prof.cyber@mycampus.ma").orElse(null);
            if (chefIlcs != null) {
                ilcs.setChefFiliere(chefIlcs);
                filiereRepository.save(ilcs);
                log.info("Set chef for ILCS filiere: {}", chefIlcs.getEmail());
            }
        }
        
        if (gl != null && gl.getChefFiliere() == null) {
            Professeur chefGl = (Professeur) utilisateurRepository.findByEmail("prof.gl@mycampus.ma").orElse(null);
            if (chefGl != null) {
                gl.setChefFiliere(chefGl);
                filiereRepository.save(gl);
                log.info("Set chef for GL filiere: {}", chefGl.getEmail());
            }
        }
        
        if (gind != null && gind.getChefFiliere() == null) {
            Professeur chefGind = (Professeur) utilisateurRepository.findByEmail("prof.indus@mycampus.ma").orElse(null);
            if (chefGind != null) {
                gind.setChefFiliere(chefGind);
                filiereRepository.save(gind);
                log.info("Set chef for GIND filiere: {}", chefGind.getEmail());
            }
        }
        
        if (geii != null && geii.getChefFiliere() == null) {
            Professeur chefGeii = (Professeur) utilisateurRepository.findByEmail("prof.elec@mycampus.ma").orElse(null);
            if (chefGeii != null) {
                geii.setChefFiliere(chefGeii);
                filiereRepository.save(geii);
                log.info("Set chef for GEII filiere: {}", chefGeii.getEmail());
            }
        }
        
        if (isi != null && isi.getChefFiliere() == null) {
            Professeur chefIsi = (Professeur) utilisateurRepository.findByEmail("prof.reseau@mycampus.ma").orElse(null);
            if (chefIsi != null) {
                isi.setChefFiliere(chefIsi);
                filiereRepository.save(isi);
                log.info("Set chef for ISI filiere: {}", chefIsi.getEmail());
            }
        }
    }*/
}
