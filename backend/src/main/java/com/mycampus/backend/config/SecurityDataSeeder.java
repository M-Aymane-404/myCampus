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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityDataSeeder {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfesseursSeeder professeursSeeder;
    private final EtudiantsSeeder etudiantsSeeder;
    private final PedagogiqueDataSeeder  pedagogiqueDataSeeder;

    @Bean
    public ApplicationRunner securityDataSeederRunner() {
        return args -> {
            log.info("Starting security data seeding...");
            seedSecurityData();
            professeursSeeder.seedProfesseurs();
            etudiantsSeeder.seedEtudiants();
            pedagogiqueDataSeeder.seedPedagogicalData();

            log.info("Security data seeding completed successfully");
        };
    }

    @Transactional
    public void seedSecurityData() {
        seedRoles();
        seedPermissions();
        seedRolePermissions();
        seedAdmins();
    }
    
    private void seedRoles() {
        Arrays.stream(RolesCode.values())
            .filter(roleCode -> !roleRepository.existsByNomRole(roleCode))
            .forEach(roleCode -> {
                Role role = Role.builder()
                    .nomRole(roleCode)
                    .description("Role " + roleCode.name())
                    .actif(true)
                    .dateCreation(LocalDateTime.now())
                    .build();
                roleRepository.save(role);
                log.info("Created role: {}", roleCode);
            });
    }
    
    private void seedPermissions() {
        List<String> permissions = Arrays.asList(
            // Security permissions
            "SECURITE_ROLE_LIRE", "SECURITE_ROLE_CREER", "SECURITE_ROLE_MODIFIER", "SECURITE_ROLE_SUPPRIMER",
            "SECURITE_PERMISSION_LIRE", "SECURITE_PERMISSION_CREER", "SECURITE_PERMISSION_MODIFIER", "SECURITE_PERMISSION_SUPPRIMER",
            "SECURITE_ROLE_PERMISSION_ATTRIBUER", "SECURITE_ROLE_PERMISSION_RETIRER", "SECURITE_ROLE_PERMISSION_LIRE",
            "SECURITE_UTILISATEUR_ROLE_ATTRIBUER", "SECURITE_UTILISATEUR_ROLE_RETIRER", "SECURITE_UTILISATEUR_ROLE_LIRE",
            
            // Student permissions
            "ETUDIANT_INFORMATION_PERSONNEL_LIRE", "ETUDIANT_MODULES_LIRE", "ETUDIANT_MATIERES_LIRE","ETUDIANT_INFORMATION_PERSONNEL_MODIFIER",
            "ETUDIANT_SEANCES_LIRE", "ETUDIANT_RESSOURCES_LIRE", "ETUDIANT_PRESENCES_LIRE", "ETUDIANT_RELEVE_LIRE","ETUDIANT_ANNONCES_LIRE","ETUDIANT_TABLEAU_DE_BORD_LIRE","ETUDIANT_FICHIER_TELECHARGER",
            
            // Professor permissions
            "PROFESSEUR_INFORMATION_PERSONNEL_LIRE", "PROFESSEUR_MODULES_LIRE", "PROFESSEUR_MATIERES_LIRE",
            "PROFESSEUR_SEANCES_LIRE", "PROFESSEUR_RESSOURCES_LIRE", "PROFESSEUR_PRESENCES_LIRE",
            "PROFESSEUR_NOTES_GERER", "PROFESSEUR_ABSENCES_GERER","PROFESSEUR_ANNONCES_GERER",
            
            // Admin structure permissions
            "ADMIN_STRUCTURE_GERER",
            
            // Admin user permissions
            "ADMIN_UTILISATEUR_GERER",
            
            // Admin security permissions
            "ADMIN_SECURITE_GERER"
        );
        
        permissions.stream()
            .filter(code -> !permissionRepository.existsByCodePermission(code))
            .forEach(code -> {
                Permission permission = Permission.builder()
                    .codePermission(code)
                    .description("Permission: " + code)
                    .dateCreation(LocalDateTime.now())
                    .build();
                permissionRepository.save(permission);
                log.info("Created permission: {}", code);
            });
    }

    private void seedRolePermissions() {
        // Admin role gets all permissions
        Role adminRole = requireRole(RolesCode.ADMIN);
        List<Permission> allPermissions = permissionRepository.findAll();
        
        for (Permission permission : allPermissions) {
            boolean exists = rolePermissionRepository
                    .existsByRole_NomRoleAndPermission_CodePermission(
                            RolesCode.ADMIN, permission.getCodePermission()
                    );

            if (!exists) {
                rolePermissionRepository.save(RolePermission.builder()
                        .role(adminRole)
                        .permission(permission)
                        .dateAttribution(LocalDateTime.now())
                        .build());
                log.info("Assigned {} to ADMIN", permission.getCodePermission());
            }
        }

        // Student role permissions
        Role studentRole = requireRole(RolesCode.ETUDIANT);
        List<String> studentPermissionCodes = Arrays.asList(

            "ETUDIANT_INFORMATION_PERSONNEL_LIRE", "ETUDIANT_MODULES_LIRE", "ETUDIANT_MATIERES_LIRE","ETUDIANT_INFORMATION_PERSONNEL_MODIFIER",
            "ETUDIANT_SEANCES_LIRE", "ETUDIANT_RESSOURCES_LIRE", "ETUDIANT_PRESENCES_LIRE", "ETUDIANT_RELEVE_LIRE","ETUDIANT_ANNONCES_LIRE","ETUDIANT_TABLEAU_DE_BORD_LIRE","ETUDIANT_FICHIER_TELECHARGER"

        );
        
        for (String code : studentPermissionCodes) {
            permissionRepository.findByCodePermission(code).ifPresent(permission -> {
                boolean exists = rolePermissionRepository
                        .existsByRole_NomRoleAndPermission_CodePermission(
                                RolesCode.ETUDIANT, code
                        );
                
                if (!exists) {
                    rolePermissionRepository.save(RolePermission.builder()
                            .role(studentRole)
                            .permission(permission)
                            .dateAttribution(LocalDateTime.now())
                            .build());
                    log.info("Assigned {} to ETUDIANT", code);
                }
            });
        }

        // Professor role permissions
        Role professeurRole = requireRole(RolesCode.PROFESSEUR);
        List<String> professeurPermissionCodes = Arrays.asList(
            "PROFESSEUR_INFORMATION_PERSONNEL_LIRE", "PROFESSEUR_MODULES_LIRE", "PROFESSEUR_MATIERES_LIRE",
            "PROFESSEUR_SEANCES_LIRE", "PROFESSEUR_RESSOURCES_LIRE", "PROFESSEUR_PRESENCES_LIRE",
            "PROFESSEUR_NOTES_GERER", "PROFESSEUR_ABSENCES_GERER","PROFESSEUR_ANNONCES_GERER"
        );
        
        for (String code : professeurPermissionCodes) {
            permissionRepository.findByCodePermission(code).ifPresent(permission -> {
                boolean exists = rolePermissionRepository
                        .existsByRole_NomRoleAndPermission_CodePermission(
                                RolesCode.PROFESSEUR, code
                        );
                
                if (!exists) {
                    rolePermissionRepository.save(RolePermission.builder()
                            .role(professeurRole)
                            .permission(permission)
                            .dateAttribution(LocalDateTime.now())
                            .build());
                    log.info("Assigned {} to PROFESSEUR", code);
                }
            });
        }
    }
    private Role requireRole(RolesCode roleCode) {
        return roleRepository.findByNomRole(roleCode)
                .orElseThrow(() -> new RuntimeException("Role " + roleCode + " not found"));
    }

    private void seedAdmins() {
        Role adminRole = roleRepository.findByNomRole(RolesCode.ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
        
        List<Administrateur> admins = Arrays.asList(
            Administrateur.builder()
                .nom("Admin1").prenom("System1").email("admin1@mycampus.ma")
                .password(passwordEncoder.encode("password123"))
                .active(true).cin("AD100001").grade("Grade A")
                .dateCreation(LocalDateTime.now()).fonction("Directeur")
                .ppr("ADM00001").tel("0600000001").build(),
            Administrateur.builder()
                .nom("Admin2").prenom("System2").email("admin2@mycampus.ma")
                    .password(passwordEncoder.encode("password123"))
                .active(true).cin("AD100002").grade("Grade B")
                .dateCreation(LocalDateTime.now()).fonction("Directeur Adjoint")
                .ppr("ADM00002").tel("0600000002").build()
        );
        
        for (Administrateur admin : admins) {
            if (!utilisateurRepository.existsByEmail(admin.getEmail())) {
                if (admin.getUtilisateurRoles() == null) {
                    admin.setUtilisateurRoles(new ArrayList<>());
                }
                
                UtilisateurRole utilisateurRole = UtilisateurRole.builder()
                    .utilisateur(admin)
                    .role(adminRole)
                    .dateAttribution(LocalDateTime.now())
                    .actif(true)
                    .build();
                
                admin.getUtilisateurRoles().add(utilisateurRole);
                utilisateurRepository.save(admin);
                log.info("Created admin user: {}", admin.getEmail());
            }
        }
    }
}