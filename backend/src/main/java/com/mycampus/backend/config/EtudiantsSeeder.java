package com.mycampus.backend.config;

import com.mycampus.backend.entites.*;
import com.mycampus.backend.enumerations.RolesCode;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.EtatInscription;
import com.mycampus.backend.repositorys.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EtudiantsSeeder {

    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ClasseRepository classeRepository;
    private final FiliereRepository filiereRepository;
    private final DepartementRepository departementRepository;
    private final PasswordEncoder passwordEncoder;



    @Transactional
    public void seedEtudiants() {
        Role etudiantRole = roleRepository.findByNomRole(RolesCode.ETUDIANT)
                .orElseThrow(() -> new RuntimeException("ETUDIANT role not found"));
        
        // Create structure in correct order: departments -> filieres -> classes
       // seedDepartements();
        seedFilieres();
        seedClasses();
        
        List<Etudiant> etudiants = Arrays.asList(
            Etudiant.builder()
                .nom("Norhane").prenom("Student").email("etud100@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100100").apogee("APO300100")
                .dateCreation(LocalDateTime.now()).tel("0600000100")
                .niveau("1ère année").anneeInscription(2024)
                .semestreActuel(Semestre.S1).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2004, 5, 15))
                .classe(requireClasse("ILCS1-A"))
                .build(),
            Etudiant.builder()
                .nom("Aymane").prenom("One").email("etud101@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100101").apogee("APO300101")
                .dateCreation(LocalDateTime.now()).tel("0600000101")
                .niveau("1ère année").anneeInscription(2024)
                .semestreActuel(Semestre.S1).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2004, 3, 20))
                .classe(requireClasse("ILCS1-A"))
                .build(),
            Etudiant.builder()
                .nom("Alae").prenom("Two").email("etud102@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100102").apogee("APO300102")
                .dateCreation(LocalDateTime.now()).tel("0600000102")
                .niveau("1ère année").anneeInscription(2024)
                .semestreActuel(Semestre.S1).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2004, 7, 8))
                .classe(requireClasse("ILCS1-B"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Three").email("etud103@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100103").apogee("APO300103")
                .dateCreation(LocalDateTime.now()).tel("0600000103")
                .niveau("1ère année").anneeInscription(2024)
                .semestreActuel(Semestre.S1).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2004, 11, 12))
                .classe(requireClasse("GL1-A"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Four").email("etud104@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100104").apogee("APO300104")
                .dateCreation(LocalDateTime.now()).tel("0600000104")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 9, 25))
                .classe(requireClasse("ISI2-A"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Five").email("etud105@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100105").apogee("APO300105")
                .dateCreation(LocalDateTime.now()).tel("0600000105")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 6, 18))
                .classe(requireClasse("ISI2-A"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Six").email("etud106@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100106").apogee("APO300106")
                .dateCreation(LocalDateTime.now()).tel("0600000106")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 12, 3))
                .classe(requireClasse("ISI2-B"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Seven").email("etud107@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100107").apogee("APO300107")
                .dateCreation(LocalDateTime.now()).tel("0600000107")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 4, 30))
                .classe(requireClasse("GL2-A"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Eight").email("etud108@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100108").apogee("APO300108")
                .dateCreation(LocalDateTime.now()).tel("0600000108")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 8, 14))
                .classe(requireClasse("GIND2-A"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Nine").email("etud109@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100109").apogee("APO300109")
                .dateCreation(LocalDateTime.now()).tel("0600000109")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 2, 27))
                .classe(requireClasse("GEII2-A"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Ten").email("etud110@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100110").apogee("APO300110")
                .dateCreation(LocalDateTime.now()).tel("0600000110")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 10, 5))
                .classe(requireClasse("ISI2-A"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Eleven").email("etud111@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100111").apogee("APO300111")
                .dateCreation(LocalDateTime.now()).tel("0600000111")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 7, 22))
                .classe(requireClasse("ISI2-B"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Twelve").email("etud112@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100112").apogee("APO300112")
                .dateCreation(LocalDateTime.now()).tel("0600000112")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 5, 19))
                .classe(requireClasse("GL2-A"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Thirteen").email("etud113@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100113").apogee("APO300113")
                .dateCreation(LocalDateTime.now()).tel("0600000113")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 9, 8))
                .classe(requireClasse("GIND2-A"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Fourteen").email("etud114@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100114").apogee("APO300114")
                .dateCreation(LocalDateTime.now()).tel("0600000114")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 3, 16))
                .classe(requireClasse("GEII2-A"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Fifteen").email("etud115@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100115").apogee("APO300115")
                .dateCreation(LocalDateTime.now()).tel("0600000115")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 11, 29))
                .classe(requireClasse("ISI2-A"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Sixteen").email("etud116@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100116").apogee("APO300116")
                .dateCreation(LocalDateTime.now()).tel("0600000116")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 6, 11))
                .classe(requireClasse("ISI2-B"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Seventeen").email("etud117@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100117").apogee("APO300117")
                .dateCreation(LocalDateTime.now()).tel("0600000117")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 8, 24))
                .classe(requireClasse("GL2-A"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Eighteen").email("etud118@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100118").apogee("APO300118")
                .dateCreation(LocalDateTime.now()).tel("0600000118")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 4, 7))
                .classe(requireClasse("GIND2-A"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Nineteen").email("etud119@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100119").apogee("APO300119")
                .dateCreation(LocalDateTime.now()).tel("0600000119")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 12, 15))
                .classe(requireClasse("GEII2-A"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("Twenty").email("etud120@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100120").apogee("APO300120")
                .dateCreation(LocalDateTime.now()).tel("0600000120")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 2, 28))
                .classe(requireClasse("ISI2-A"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("TwentyOne").email("etud121@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100121").apogee("APO300121")
                .dateCreation(LocalDateTime.now()).tel("0600000121")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 10, 12))
                .classe(requireClasse("ISI2-B"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("TwentyTwo").email("etud122@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100122").apogee("APO300122")
                .dateCreation(LocalDateTime.now()).tel("0600000122")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 5, 31))
                .classe(requireClasse("GL2-A"))
                .build(),
            Etudiant.builder()
                .nom("Etudiant").prenom("TwentyThree").email("etud123@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100123").apogee("APO300123")
                .dateCreation(LocalDateTime.now()).tel("0600000123")
                .niveau("3ème année").anneeInscription(2022)
                .semestreActuel(Semestre.S3).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2002, 7, 19))
                .classe(requireClasse("GIND2-A"))
                .build()
        );
        
        for (Etudiant etudiant : etudiants) {
            if (!utilisateurRepository.existsByEmail(etudiant.getEmail())) {
                if (etudiant.getUtilisateurRoles() == null) {
                    etudiant.setUtilisateurRoles(new ArrayList<>());
                }
                
                UtilisateurRole utilisateurRole = UtilisateurRole.builder()
                    .utilisateur(etudiant)
                    .role(etudiantRole)
                    .dateAttribution(LocalDateTime.now())
                    .actif(true)
                    .build();
                
                etudiant.getUtilisateurRoles().add(utilisateurRole);
                utilisateurRepository.save(etudiant);
                log.info("Created student: {}", etudiant.getEmail());
            }
        }
    }

    private void seedDepartements() {
        List<Departement> departements = Arrays.asList(
            Departement.builder()
                .codeDepartement("INFO").typeDepartement("Département d'Informatique")
                .description("Département d'Informatique")
                .etat(com.mycampus.backend.enumerations.EtatStructure.OUVERTE)
                .dateCreation(LocalDate.now())
                .build(),
            Departement.builder()
                .codeDepartement("INDUS").typeDepartement("Département de Génie Industriel")
                .description("Département de Génie Industriel")
                .etat(com.mycampus.backend.enumerations.EtatStructure.OUVERTE)
                .dateCreation(LocalDate.now())
                .build(),
            Departement.builder()
                .codeDepartement("ELEC").typeDepartement("Département de Génie Électrique")
                .description("Département de Génie Électrique")
                .etat(com.mycampus.backend.enumerations.EtatStructure.OUVERTE)
                .dateCreation(LocalDate.now())
                .build()
        );
        
        for (Departement dept : departements) {
            if (!departementRepository.existsByCodeDepartement(dept.getCodeDepartement())) {
                departementRepository.save(dept);
                log.info("Created department: {}", dept.getCodeDepartement());
            }
        }
    }

    private void seedFilieres() {
        Departement infoDept = requireDepartement("INFO");
        Departement indusDept = requireDepartement("INDUS");
        Departement elecDept = requireDepartement("ELEC");
        
        List<Filiere> filieres = Arrays.asList(
            Filiere.builder()
                .codeFiliere("ILCS").nom("Ingénierie Logicielle et Conception de Systèmes")
                .description("Filière ILCS")
                .dureeFormation("5 ans").niveauDiplome("Ingénieur")
                .etat(com.mycampus.backend.enumerations.EtatStructure.OUVERTE)
                .departement(infoDept)
                .build(),
            Filiere.builder()
                .codeFiliere("GL").nom("Génie Logiciel")
                .description("Filière Génie Logiciel")
                .dureeFormation("5 ans").niveauDiplome("Ingénieur")
                .etat(com.mycampus.backend.enumerations.EtatStructure.OUVERTE)
                .departement(infoDept)
                .build(),
            Filiere.builder()
                .codeFiliere("GIND").nom("Génie Industriel")
                .description("Filière Génie Industriel")
                .dureeFormation("5 ans").niveauDiplome("Ingénieur")
                .etat(com.mycampus.backend.enumerations.EtatStructure.OUVERTE)
                .departement(indusDept)
                .build(),
            Filiere.builder()
                .codeFiliere("GEII").nom("Génie Électrique et Informatique Industrielle")
                .description("Filière GEII")
                .dureeFormation("5 ans").niveauDiplome("Ingénieur")
                .etat(com.mycampus.backend.enumerations.EtatStructure.OUVERTE)
                .departement(elecDept)
                .build(),
            Filiere.builder()
                .codeFiliere("ISI").nom("Informatique et Systèmes d'Information")
                .description("Filière ISI")
                .dureeFormation("5 ans").niveauDiplome("Ingénieur")
                .etat(com.mycampus.backend.enumerations.EtatStructure.OUVERTE)
                .departement(infoDept)
                .build()
        );
        
        for (Filiere filiere : filieres) {
            if (!filiereRepository.existsByCodeFiliere(filiere.getCodeFiliere())) {
                filiereRepository.save(filiere);
                log.info("Created filiere: {}", filiere.getCodeFiliere());
            }
        }
    }

    private void seedClasses() {
        Filiere ilcs = requireFiliere("ILCS");
        Filiere gl = requireFiliere("GL");
        Filiere gind = requireFiliere("GIND");
        Filiere geii = requireFiliere("GEII");
        Filiere isi = requireFiliere("ISI");
        
        List<Classe> classes = Arrays.asList(
            Classe.builder().codeClasse("ILCS1-A").nom("ILCS1-A")
                .niveau("1ère année").anneeUniversitaire("2024-2025")
                .effectifMax(30).filiere(ilcs)
                .build(),
            Classe.builder().codeClasse("ILCS1-B").nom("ILCS1-B")
                .niveau("1ère année").anneeUniversitaire("2024-2025")
                .effectifMax(30).filiere(ilcs)
                .build(),
            Classe.builder().codeClasse("GL1-A").nom("GL1-A")
                .niveau("1ère année").anneeUniversitaire("2024-2025")
                .effectifMax(30).filiere(gl)
                .build(),
            Classe.builder().codeClasse("GL2-A").nom("GL2-A")
                .niveau("2ème année").anneeUniversitaire("2024-2025")
                .effectifMax(30).filiere(gl)
                .build(),
            Classe.builder().codeClasse("GIND1-A").nom("GIND1-A")
                .niveau("1ère année").anneeUniversitaire("2024-2025")
                .effectifMax(30).filiere(gind)
                .build(),
            Classe.builder().codeClasse("GIND2-A").nom("GIND2-A")
                .niveau("2ème année").anneeUniversitaire("2024-2025")
                .effectifMax(30).filiere(gind)
                .build(),
            Classe.builder().codeClasse("GEII2-A").nom("GEII2-A")
                .niveau("2ème année").anneeUniversitaire("2024-2025")
                .effectifMax(30).filiere(geii)
                .build(),
            Classe.builder().codeClasse("ISI2-A").nom("ISI2-A")
                .niveau("2ème année").anneeUniversitaire("2024-2025")
                .effectifMax(30).filiere(isi)
                .build(),
            Classe.builder().codeClasse("ISI2-B").nom("ISI2-B")
                .niveau("2ème année").anneeUniversitaire("2024-2025")
                .effectifMax(30).filiere(isi)
                .build()
        );
        
        for (Classe classe : classes) {
            if (!classeRepository.existsByCodeClasse(classe.getCodeClasse())) {
                classeRepository.save(classe);
                log.info("Created class: {}", classe.getCodeClasse());
            }
        }
    }

    private Departement requireDepartement(String codeDepartement) {
        return departementRepository.findByCodeDepartement(codeDepartement)
                .orElseThrow(() -> new RuntimeException("Departement " + codeDepartement + " not found during student seeding"));
    }

    private Filiere requireFiliere(String codeFiliere) {
        return filiereRepository.findByCodeFiliere(codeFiliere)
                .orElseThrow(() -> new RuntimeException("Filiere " + codeFiliere + " not found during student seeding"));
    }

    private Classe requireClasse(String codeClasse) {
        return classeRepository.findByCodeClasse(codeClasse)
                .orElseThrow(() -> new RuntimeException("Classe " + codeClasse + " not found during student seeding"));
    }
}
