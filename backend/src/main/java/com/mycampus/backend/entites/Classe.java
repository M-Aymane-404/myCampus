package com.mycampus.backend.entites;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"filiere", "etudiants", "affectationEnseignements"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Classe {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true, updatable = false)
    private String codeClasse;
    
    @Column(nullable = false)
    private String nom;
    
    @Column(nullable = false)
    private String niveau;
    
    @Column(nullable = false)
    private String anneeUniversitaire;
    
    @Column(nullable = false)
    private Integer effectifMax;

    private String emploiTemps;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filiere_id", nullable = false)
    private Filiere filiere;
    
    @OneToMany(mappedBy = "classe")
    private List<Etudiant> etudiants;
    
    @OneToMany(mappedBy = "classe")
    private List<AffectationEnseignement> affectationEnseignements;
}
