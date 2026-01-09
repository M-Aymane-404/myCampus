package com.mycampus.backend.dto;

import com.mycampus.backend.entites.Notification;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;

import java.time.LocalDateTime;
import java.util.List;

public class UtilisateurDto {
    private Long id;

    private String nom;

    private String prenom;

    private String email;

    private String password;

    private String cin;

    private String tel;

    private String photo;

    private Boolean enabled ;

    private LocalDateTime dateCreation;

    private LocalDateTime dateDerniereConnexion;


}
