package com.mycampus.backend.entites;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"filiere", "etudiants", "affectationEnseignements"})
@EqualsAndHashCode(exclude = {"filiere", "etudiants", "affectationEnseignements"})
public class Classe {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String codeClasse;
    
    @Column(nullable = false)
    private String nom;
    
    @Column(nullable = false)
    private String niveau;
    
    @Column(nullable = false)
    private String anneeUniversitaire;
    
    @Column(nullable = false)
    private Integer effectifMax;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filiere_id", nullable = false)
    private Filiere filiere;
    
    @OneToMany(mappedBy = "classe")
    private List<Etudiant> etudiants;
    
    @OneToMany(mappedBy = "classe")
    private List<AffectationEnseignement> affectationEnseignements;
}
