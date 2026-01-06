package com.mycampus.backend;

import com.mycampus.backend.Entities.Client;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }


    @Bean
    CommandLineRunner start() {
        return args -> {
            Client.builder()
                    .prenom("Kamal")
                    .nom("Alami")
                    .age(30)
                    .build();
        };
    }
















}
