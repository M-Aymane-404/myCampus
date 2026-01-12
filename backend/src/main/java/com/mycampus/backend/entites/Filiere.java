package com.mycampus.backend.entites;

import jakarta.persistence.*;
import lombok.*;
import com.mycampus.backend.enumerations.EtatStructure;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"departement", "classes", "modules", "chefFiliere"})
@EqualsAndHashCode(exclude = {"departement", "classes", "modules", "chefFiliere"})
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id", nullable = false)
    private Departement departement;
    
    @OneToMany(mappedBy = "filiere", fetch = FetchType.LAZY)
    private List<Classe> classes;

    @OneToMany(mappedBy = "filiere", fetch = FetchType.LAZY)
    private List<Module> modules;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "chef_filiere_id", nullable = true, unique = true)
    private Professeur chefFiliere;

}
