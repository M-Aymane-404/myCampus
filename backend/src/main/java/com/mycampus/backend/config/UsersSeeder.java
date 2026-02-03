package com.mycampus.backend.config;

import com.mycampus.backend.entites.*;
import com.mycampus.backend.enumerations.RolesCode;
 import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.repositorys.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UsersSeeder {


    /*

    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DepartementRepository departementRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner usersSeederRunner() {
        return args -> {
            log.info("Starting users data seeding...");
            seedUsers();
            log.info("Users data seeding completed successfully");
        };
    }

    @Transactional
    public void seedUsers() {
        seedProfesseurs();
        seedEtudiants();
    }

    private void seedProfesseurs() {
        Role professeurRole = roleRepository.findByNomRole(RolesCode.PROFESSEUR)
                .orElseThrow(() -> new RuntimeException("PROFESSEUR role not found"));
        
        // Create departments first for chef assignments
        seedDepartements();
        
        List<Professeur> professeurs = Arrays.asList(
            Professeur.builder()
                .id(10L).nom("Professeur").prenom("Cyber").email("prof.cyber@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("PC100010").ppr("PPR200010")
                .specialite("Cybersécurité").statut(StatutProfesseur.TITULAIRE).grade("Professeur")
                .dateCreation(LocalDateTime.now()).tel("0600000010")
                .departement(departementRepository.findByCode("INFO").orElse(null))
                .build(),
            Professeur.builder()
                .id(11L).nom("Professeur").prenom("Math").email("prof.math@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("PC100011").ppr("PPR200011")
                .specialite("Mathématiques").statut(StatutProfesseur.TITULAIRE).grade("Professeur")
                .dateCreation(LocalDateTime.now()).tel("0600000011")
                .departement(departementRepository.findByCode("INFO").orElse(null))
                .build(),
            Professeur.builder()
                .id(12L).nom("Professeur").prenom("Indus").email("prof.indus@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("PC100012").ppr("PPR200012")
                .specialite("Génie Industriel").statut(StatutProfesseur.TITULAIRE).grade("Professeur")
                .dateCreation(LocalDateTime.now()).tel("0600000012")
                .departement(departementRepository.findByCode("INDUS").orElse(null))
                .build(),
            Professeur.builder()
                .id(13L).nom("Professeur").prenom("GL").email("prof.gl@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("PC100013").ppr("PPR200013")
                .specialite("Génie Logiciel").statut(StatutProfesseur.TITULAIRE).grade("Professeur")
                .dateCreation(LocalDateTime.now()).tel("0600000013")
                .departement(departementRepository.findByCode("INFO").orElse(null))
                .build(),
            Professeur.builder()
                .id(14L).nom("Professeur").prenom("Web").email("prof.web@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("PC100014").ppr("PPR200014")
                .specialite("Web Development").statut(StatutProfesseur.TITULAIRE).grade("Professeur")
                .dateCreation(LocalDateTime.now()).tel("0600000014")
                .departement(departementRepository.findByCode("INFO").orElse(null))
                .build(),
            Professeur.builder()
                .id(15L).nom("Professeur").prenom("Reseau").email("prof.reseau@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("PC100015").ppr("PPR200015")
                .specialite("Réseaux").statut(StatutProfesseur.TITULAIRE).grade("Professeur")
                .dateCreation(LocalDateTime.now()).tel("0600000015")
                .departement(departementRepository.findByCode("INFO").orElse(null))
                .build(),
            Professeur.builder()
                .id(16L).nom("Professeur").prenom("Elec").email("prof.elec@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("PC100016").ppr("PPR200016")
                .specialite("Électronique").statut(StatutProfesseur.TITULAIRE).grade("Professeur")
                .dateCreation(LocalDateTime.now()).tel("0600000016")
                .departement(departementRepository.findByCode("ELEC").orElse(null))
                .build(),
            Professeur.builder()
                .id(17L).nom("Professeur").prenom("Physique").email("prof.physique@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("PC100017").ppr("PPR200017")
                .specialite("Physique").statut(StatutProfesseur.TITULAIRE).grade("Professeur")
                .dateCreation(LocalDateTime.now()).tel("0600000017")
                .departement(departementRepository.findByCode("INDUS").orElse(null))
                .build()
        );
        
        for (Professeur prof : professeurs) {
            if (!utilisateurRepository.existsByEmail(prof.getEmail())) {
                if (prof.getUtilisateurRoles() == null) {
                    prof.setUtilisateurRoles(new ArrayList<>());
                }
                
                UtilisateurRole utilisateurRole = UtilisateurRole.builder()
                    .utilisateur(prof)
                    .role(professeurRole)
                    .dateAttribution(LocalDateTime.now())
                    .actif(true)
                    .build();
                
                prof.getUtilisateurRoles().add(utilisateurRole);
                utilisateurRepository.save(prof);
                log.info("Created professor: {}", prof.getEmail());
            }
        }
        
        // Set department chefs
        setDepartmentChefs();
    }

    private void seedEtudiants() {
        Role etudiantRole = roleRepository.findByNomRole(RolesCode.ETUDIANT)
                .orElseThrow(() -> new RuntimeException("ETUDIANT role not found"));
        
        List<Etudiant> etudiants = Arrays.asList(
            Etudiant.builder()
                .id(100L).nom("Norhane").prenom("Student").email("etud100@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100100").apogee("APO300100")
                .dateCreation(LocalDateTime.now()).tel("0600000100")
                .semestreActuel(Semestre.S1)
                .build(),
            Etudiant.builder()
                .id(101L).nom("Etudiant").prenom("One").email("etud101@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100101").apogee("APO300101")
                .dateCreation(LocalDateTime.now()).tel("0600000101")
                .semestreActuel(Semestre.S1)
                .build(),
            Etudiant.builder()
                .id(102L).nom("Etudiant").prenom("Two").email("etud102@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100102").apogee("APO300102")
                .dateCreation(LocalDateTime.now()).tel("0600000102")
                .semestreActuel(Semestre.S1)
                .build(),
            Etudiant.builder()
                .id(103L).nom("Etudiant").prenom("Three").email("etud103@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100103").apogee("APO300103")
                .dateCreation(LocalDateTime.now()).tel("0600000103")
                .semestreActuel(Semestre.S1)
                .build(),
            Etudiant.builder()
                .id(104L).nom("Etudiant").prenom("Four").email("etud104@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100104").apogee("APO300104")
                .dateCreation(LocalDateTime.now()).tel("0600000104")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(105L).nom("Etudiant").prenom("Five").email("etud105@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100105").apogee("APO300105")
                .dateCreation(LocalDateTime.now()).tel("0600000105")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(106L).nom("Etudiant").prenom("Six").email("etud106@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100106").apogee("APO300106")
                .dateCreation(LocalDateTime.now()).tel("0600000106")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(107L).nom("Etudiant").prenom("Seven").email("etud107@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100107").apogee("APO300107")
                .dateCreation(LocalDateTime.now()).tel("0600000107")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(108L).nom("Etudiant").prenom("Eight").email("etud108@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100108").apogee("APO300108")
                .dateCreation(LocalDateTime.now()).tel("0600000108")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(109L).nom("Etudiant").prenom("Nine").email("etud109@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100109").apogee("APO300109")
                .dateCreation(LocalDateTime.now()).tel("0600000109")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(110L).nom("Etudiant").prenom("Ten").email("etud110@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100110").apogee("APO300110")
                .dateCreation(LocalDateTime.now()).tel("0600000110")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(111L).nom("Etudiant").prenom("Eleven").email("etud111@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100111").apogee("APO300111")
                .dateCreation(LocalDateTime.now()).tel("0600000111")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(112L).nom("Etudiant").prenom("Twelve").email("etud112@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100112").apogee("APO300112")
                .dateCreation(LocalDateTime.now()).tel("0600000112")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(113L).nom("Etudiant").prenom("Thirteen").email("etud113@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100113").apogee("APO300113")
                .dateCreation(LocalDateTime.now()).tel("0600000113")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(114L).nom("Etudiant").prenom("Fourteen").email("etud114@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100114").apogee("APO300114")
                .dateCreation(LocalDateTime.now()).tel("0600000114")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(115L).nom("Etudiant").prenom("Fifteen").email("etud115@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100115").apogee("APO300115")
                .dateCreation(LocalDateTime.now()).tel("0600000115")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(116L).nom("Etudiant").prenom("Sixteen").email("etud116@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100116").apogee("APO300116")
                .dateCreation(LocalDateTime.now()).tel("0600000116")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(117L).nom("Etudiant").prenom("Seventeen").email("etud117@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100117").apogee("APO300117")
                .dateCreation(LocalDateTime.now()).tel("0600000117")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(118L).nom("Etudiant").prenom("Eighteen").email("etud118@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100118").apogee("APO300118")
                .dateCreation(LocalDateTime.now()).tel("0600000118")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(119L).nom("Etudiant").prenom("Nineteen").email("etud119@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100119").apogee("APO300119")
                .dateCreation(LocalDateTime.now()).tel("0600000119")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(120L).nom("Etudiant").prenom("Twenty").email("etud120@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100120").apogee("APO300120")
                .dateCreation(LocalDateTime.now()).tel("0600000120")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(121L).nom("Etudiant").prenom("TwentyOne").email("etud121@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100121").apogee("APO300121")
                .dateCreation(LocalDateTime.now()).tel("0600000121")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(122L).nom("Etudiant").prenom("TwentyTwo").email("etud122@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100122").apogee("APO300122")
                .dateCreation(LocalDateTime.now()).tel("0600000122")
                .semestreActuel(Semestre.S3)
                .build(),
            Etudiant.builder()
                .id(123L).nom("Etudiant").prenom("TwentyThree").email("etud123@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100123").apogee("APO300123")
                .dateCreation(LocalDateTime.now()).tel("0600000123")
                .semestreActuel(Semestre.S3)
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
                .code("INFO").nom("Informatique").description("Département d'Informatique")
                .etat(EtatDepartement.OUVERTE).dateCreation(LocalDateTime.now())
                .build(),
            Departement.builder()
                .code("INDUS").nom("Génie Industriel").description("Département de Génie Industriel")
                .etat(EtatDepartement.OUVERTE).dateCreation(LocalDateTime.now())
                .build(),
            Departement.builder()
                .code("ELEC").nom("Génie Électrique").description("Département de Génie Électrique")
                .etat(EtatDepartement.OUVERTE).dateCreation(LocalDateTime.now())
                .build()
        );
        
        for (Departement dept : departements) {
            if (!departementRepository.existsByCode(dept.getCode())) {
                departementRepository.save(dept);
                log.info("Created department: {}", dept.getCode());
            }
        }
    }

    private void setDepartmentChefs() {
        Departement infoDept = departementRepository.findByCode("INFO").orElse(null);
        Departement indusDept = departementRepository.findByCode("INDUS").orElse(null);
        Departement elecDept = departementRepository.findByCode("ELEC").orElse(null);
        
        if (infoDept != null && infoDept.getChefDepartement() == null) {
            Professeur chefInfo = (Professeur) utilisateurRepository.findByEmail("prof.cyber@mycampus.ma").orElse(null);
            if (chefInfo != null) {
                infoDept.setChefDepartement(chefInfo);
                departementRepository.save(infoDept);
                log.info("Set chef for INFO department: {}", chefInfo.getEmail());
            }
        }
        
        if (indusDept != null && indusDept.getChefDepartement() == null) {
            Professeur chefIndus = (Professeur) utilisateurRepository.findByEmail("prof.indus@mycampus.ma").orElse(null);
            if (chefIndus != null) {
                indusDept.setChefDepartement(chefIndus);
                departementRepository.save(indusDept);
                log.info("Set chef for INDUS department: {}", chefIndus.getEmail());
            }
        }
        
        if (elecDept != null && elecDept.getChefDepartement() == null) {
            Professeur chefElec = (Professeur) utilisateurRepository.findByEmail("prof.elec@mycampus.ma").orElse(null);
            if (chefElec != null) {
                elecDept.setChefDepartement(chefElec);
                departementRepository.save(elecDept);
                log.info("Set chef for ELEC department: {}", chefElec.getEmail());
            }
        }
    }*/
}
