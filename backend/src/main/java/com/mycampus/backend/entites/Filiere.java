package com.mycampus.backend.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.mycampus.backend.enumerations.EtatStructure;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Filiere {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String codeFiliere;
    
    @Column(nullable = false)
    private String nom;
    
    private String description;
    
    private String emploiTemps;
    
    @Column(nullable = false)
    private String dureeFormation;
    
    @Column(nullable = false)
    private String niveauDiplome;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtatStructure etat;
    
    @ManyToOne
    @JoinColumn(name = "departement_id", nullable = false)
    private Departement departement;
    
    @OneToMany(mappedBy = "filiere")
    private List<Classe> classes;

    @OneToMany(mappedBy = "filiere")
    private List<Module> modules;

    @OneToOne
    @JoinColumn(name = "chef_filiere_id", unique = true)
    private Professeur chefFiliere;

}
