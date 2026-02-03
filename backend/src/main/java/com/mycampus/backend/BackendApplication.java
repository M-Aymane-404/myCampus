package com.mycampus.backend;

import com.mycampus.backend.dto.securite.entree.PermissionCreationDTO;
import com.mycampus.backend.metier.security.GestionDesRolesEtPermissionsMetier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableJpaAuditing
public class BackendApplication {

    private static final Logger logger = LoggerFactory.getLogger(BackendApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    CommandLineRunner commandLineRunner(
             GestionDesRolesEtPermissionsMetier metier
    ) {
        return args -> {
            metier.creerPermission(
                    PermissionCreationDTO.builder()
                            .codePermission("")
                            .description("")
                            .build()
            );
        };
    }






}
