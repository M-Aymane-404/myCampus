package com.mycampus.backend;

import com.mycampus.backend.Entities.Client;
import com.mycampus.backend.repositorys.IClientRepository;
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
    CommandLineRunner start(IClientRepository clientRepository) {
        return args -> {
            clientRepository.save(Client.builder()
                    .prenom("Kamal")
                    .nom("Alami")
                    .age(30)
                    .build());
            clientRepository.save(Client.builder()
                    .prenom("Ahmed")
                    .nom("Bennani")
                    .age(40)
                    .build());
            clientRepository.findAll().forEach(c->System.out.println(c));


        };
    }
















}
