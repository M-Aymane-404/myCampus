package com.mycampus.backend.Entities;

import jakarta.persistence.Entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Client{

    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotEmpty(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String nom;

    @NotEmpty(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prenom doit contenir entre 2 et 50 caractères")
    private String prenom;

    @NotNull(message = "L'âge est obligatoire")
    @Min(value = 18, message = "L'âge minimum est 18 ans")
    private int age;


}