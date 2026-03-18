package com.mycampus.backend.entites;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"annonce"})
@EqualsAndHashCode(exclude = {"annonce"})
public class RessourceAnnonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomOriginal;

    @Column(nullable = false)
    private String chemin;

    @Column(nullable = false)
    private String typeMime;

    @Column(nullable = false)
    private Long taille;

    @Column(nullable = false)
    private LocalDateTime dateUpload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annonce_id", nullable = false)
    private Annonce annonce;
}
