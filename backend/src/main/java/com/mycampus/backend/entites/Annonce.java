package com.mycampus.backend.entites;

import com.mycampus.backend.enumerations.AnnonceCibleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
 @Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Annonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)

    private String titre;
    @Column(nullable = false)
    private String contenu;
    @Column(nullable = false)
    private LocalDateTime datePublication;
     private boolean publiee;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnonceCibleType cibleType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auteur_id", nullable = false)
    private Utilisateur auteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id")
    private Classe classe;


}
