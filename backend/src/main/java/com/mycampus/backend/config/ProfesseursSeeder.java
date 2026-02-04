package com.mycampus.backend.config;

import com.mycampus.backend.entites.*;
import com.mycampus.backend.enumerations.RolesCode;
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
public class ProfesseursSeeder {

    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DepartementRepository departementRepository;
    private final PasswordEncoder passwordEncoder;



    @Transactional
    public void seedProfesseurs() {
        Role professeurRole = roleRepository.findByNomRole(RolesCode.PROFESSEUR)
                .orElseThrow(() -> new RuntimeException("PROFESSEUR role not found"));
        
        // Create departments first for chef assignments
        seedDepartements();
        
        List<Professeur> professeurs = Arrays.asList(
            Professeur.builder()
                .nom("Professeur").prenom("Cyber").email("prof.cyber@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("PC100010").ppr("PPR200010")
                .specialite("Cybersécurité").statut("Titulaire").grade("Professeur")
                .dateCreation(LocalDateTime.now()).tel("0600000010")
                .dateRecrutement(LocalDate.of(2020, 9, 1))
                .departementOuTravail(requireDepartement("INFO"))
                .build(),
            Professeur.builder()
                .nom("Professeur").prenom("Math").email("prof.math@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("PC100011").ppr("PPR200011")
                .specialite("Mathématiques").statut("Titulaire").grade("Professeur")
                .dateCreation(LocalDateTime.now()).tel("0600000011")
                .dateRecrutement(LocalDate.of(2019, 9, 1))
                .departementOuTravail(requireDepartement("INFO"))
                .build(),
            Professeur.builder()
                .nom("Professeur").prenom("Indus").email("prof.indus@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("PC100012").ppr("PPR200012")
                .specialite("Génie Industriel").statut("Titulaire").grade("Professeur")
                .dateCreation(LocalDateTime.now()).tel("0600000012")
                .dateRecrutement(LocalDate.of(2018, 9, 1))
                .departementOuTravail(requireDepartement("INDUS"))
                .build(),
            Professeur.builder()
                .nom("Professeur").prenom("GL").email("prof.gl@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("PC100013").ppr("PPR200013")
                .specialite("Génie Logiciel").statut("Titulaire").grade("Professeur")
                .dateCreation(LocalDateTime.now()).tel("0600000013")
                .dateRecrutement(LocalDate.of(2021, 9, 1))
                .departementOuTravail(requireDepartement("INFO"))
                .build(),
            Professeur.builder()
                .nom("Professeur").prenom("Web").email("prof.web@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("PC100014").ppr("PPR200014")
                .specialite("Web Development").statut("Titulaire").grade("Professeur")
                .dateCreation(LocalDateTime.now()).tel("0600000014")
                .dateRecrutement(LocalDate.of(2020, 9, 1))
                .departementOuTravail(requireDepartement("INFO"))
                .build(),
            Professeur.builder()
                .nom("Professeur").prenom("Reseau").email("prof.reseau@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("PC100015").ppr("PPR200015")
                .specialite("Réseaux").statut("Titulaire").grade("Professeur")
                .dateCreation(LocalDateTime.now()).tel("0600000015")
                .dateRecrutement(LocalDate.of(2019, 9, 1))
                .departementOuTravail(requireDepartement("INFO"))
                .build(),
            Professeur.builder()
                .nom("Professeur").prenom("Elec").email("prof.elec@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("PC100016").ppr("PPR200016")
                .specialite("Électronique").statut("Titulaire").grade("Professeur")
                .dateCreation(LocalDateTime.now()).tel("0600000016")
                .dateRecrutement(LocalDate.of(2018, 9, 1))
                .departementOuTravail(requireDepartement("ELEC"))
                .build(),
            Professeur.builder()
                .nom("Professeur").prenom("Physique").email("prof.physique@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("PC100017").ppr("PPR200017")
                .specialite("Physique").statut("Titulaire").grade("Professeur")
                .dateCreation(LocalDateTime.now()).tel("0600000017")
                .dateRecrutement(LocalDate.of(2017, 9, 1))
                .departementOuTravail(requireDepartement("INDUS"))
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

    private void setDepartmentChefs() {
        // ✅ Utilisation des méthodes requireXxx() pour garantir les dépendances
        Departement infoDept = requireDepartement("INFO");
        Departement indusDept = requireDepartement("INDUS");
        Departement elecDept = requireDepartement("ELEC");
        
        if (infoDept.getChefDepartement() == null) {
            Professeur chefInfo = requireProfesseurByEmail("prof.cyber@mycampus.ma");
            infoDept.setChefDepartement(chefInfo);
            departementRepository.save(infoDept);
            log.info("Set chef for INFO department: {}", chefInfo.getEmail());
        }
        
        if (indusDept.getChefDepartement() == null) {
            Professeur chefIndus = requireProfesseurByEmail("prof.indus@mycampus.ma");
            indusDept.setChefDepartement(chefIndus);
            departementRepository.save(indusDept);
            log.info("Set chef for INDUS department: {}", chefIndus.getEmail());
        }
        
        if (elecDept.getChefDepartement() == null) {
            Professeur chefElec = requireProfesseurByEmail("prof.elec@mycampus.ma");
            elecDept.setChefDepartement(chefElec);
            departementRepository.save(elecDept);
            log.info("Set chef for ELEC department: {}", chefElec.getEmail());
        }
    }

    // ✅ Méthodes requireXxx() défensives pour les FK obligatoires
    private Departement requireDepartement(String codeDepartement) {
        return departementRepository.findByCodeDepartement(codeDepartement)
                .orElseThrow(() -> new RuntimeException("Departement " + codeDepartement + " not found during professor seeding"));
    }

    private Professeur requireProfesseurByEmail(String email) {
        return (Professeur) utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Professeur " + email + " not found during professor seeding"));
    }
}
