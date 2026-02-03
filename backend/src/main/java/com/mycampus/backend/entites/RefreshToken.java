package com.mycampus.backend.entites;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     private String id ;
    private String token;
    private LocalDateTime dateCreation;
    private LocalDateTime dateExpiration;
    private Boolean revoque;
    private String userAgent;
    private String ip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;
}
