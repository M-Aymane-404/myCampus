package com.mycampus.backend.config;

import com.mycampus.backend.entites.*;
import com.mycampus.backend.enumerations.NiveauAnnee;
import com.mycampus.backend.enumerations.RolesCode;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.EtatInscription;
import com.mycampus.backend.repositorys.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        
        // seedDepartements();
        seedFilieres();
        seedClasses();
        
        List<Etudiant> etudiants = Arrays.asList(
            Etudiant.builder()
                .nom("Karim").prenom("Bannani").email("Karim@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("EC100100").apogee("APO300100")
                .dateCreation(LocalDateTime.now()).tel("0600000100")
                .niveau(NiveauAnnee.A1).anneeInscription(2024)
                .semestreActuel(Semestre.S1).etatInscription(EtatInscription.INSCRIT)
                .dateNaissance(LocalDate.of(2004, 5, 15))
                .classe(requireClasse("ILCS1-A"))
                .build());

        
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



    private void seedFilieres() {
        Departement infoDept = requireDepartement("INFO");

        
        List<Filiere> filieres = Arrays.asList(
            Filiere.builder()
                .codeFiliere("ILCS").nom("Ingénierie Logicielle et Conception de Systèmes")
                .description("Filière ILCS")
                .dureeFormation("2 ans").niveauDiplome("Ingénieur")
                .etat(com.mycampus.backend.enumerations.EtatStructure.OUVERTE)
                .departement(infoDept)
                .build());



        
        for (Filiere filiere : filieres) {
            if (!filiereRepository.existsByCodeFiliere(filiere.getCodeFiliere())) {
                filiereRepository.save(filiere);
                log.info("Created filiere: {}", filiere.getCodeFiliere());
            }
        }
    }

    private void seedClasses() {
        Filiere ilcs = requireFiliere("ILCS");
        List<Classe> classes = Arrays.asList(
            Classe.builder().codeClasse("ILCS1-A").nom("ILCS1-A")
                .niveau("1ère année").anneeUniversitaire("2024-2025")
                .effectifMax(30).filiere(ilcs)
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
