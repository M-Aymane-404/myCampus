package com.mycampus.backend.entites;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data

@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@ToString(exclude = {"notifications"})
@EqualsAndHashCode(exclude = {"notifications"})
public class Utilisateur {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nom;
    
    @Column(nullable = false)
    private String prenom;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, unique = true)
    private String cin;
    
    private String tel;
    
    private String photo;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    private LocalDateTime dateCreation;
    
    private LocalDateTime dateDerniereConnexion;
    
    @OneToMany(mappedBy = "utilisateur")
    private List<Notification> notifications;
}
