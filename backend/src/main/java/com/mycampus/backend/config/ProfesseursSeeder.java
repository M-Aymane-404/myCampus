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
                .nom("Ahmed").prenom("Kamal").email("prof.info1@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("PC100012").ppr("PPR200012")
                .specialite("Génie informatique").statut("Titulaire").grade("Professeur")
                .dateCreation(LocalDateTime.now()).tel("0600000012")
                .dateRecrutement(LocalDate.of(2018, 9, 1))
                .departementOuTravail(requireDepartement("INFO"))
                .build(),

                Professeur.builder()
                        .nom("Said").prenom("Barada").email("prof.info2@mycampus.ma")
                        .password(passwordEncoder.encode("password123"))
                        .active(true).cin("PC200012").ppr("PPR300012")
                        .specialite("Génie informatique").statut("Titulaire").grade("Professeur")
                        .dateCreation(LocalDateTime.now()).tel("0610000012")
                        .dateRecrutement(LocalDate.of(2017, 7, 5))
                        .departementOuTravail(requireDepartement("INFO"))
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
         Departement infoDept = requireDepartement("INFO");

        if (infoDept.getChefDepartement() == null) {
            Professeur chefInfo = requireProfesseurByEmail("prof.info1@mycampus.ma");
            infoDept.setChefDepartement(chefInfo);
            departementRepository.save(infoDept);
            log.info("Set chef for INFO department: {}", chefInfo.getEmail());
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
