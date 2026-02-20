package com.mycampus.backend.entites;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

 import java.time.LocalDateTime;
import java.util.ArrayList;
 import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"notifications"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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
    
     private String photoUrl;
    private String photoNom;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    private LocalDateTime dateCreation;
    
    private LocalDateTime dateDerniereConnexion;
    
    @OneToMany(mappedBy = "utilisateur",cascade = CascadeType.ALL)
    private List<Notification> notificationsRecoit;

    @OneToMany(mappedBy = "auteur",cascade = CascadeType.ALL)
    private List<Notification> notificationsCreer;

    @OneToMany(mappedBy = "utilisateur" ,cascade = CascadeType.ALL,    orphanRemoval = true)

    private List<UtilisateurRole> utilisateurRoles;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens;

    @OneToMany(mappedBy = "auteur",cascade = CascadeType.ALL)
    private List<Annonce>  annonces = new ArrayList<>();
}
