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
     private Long id ;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    @Column(name = "revoque", nullable = false)
    private Boolean revoque;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "ip", length = 64)
    private String ip;

    @Column(name = "token_family", nullable = false, length = 64)
    private String tokenFamily;

    @Column(name = "remplace_par_hash", length = 64)
    private String remplaceParHash;

    @Transient
    private String token;



    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;
}
