package com.mycampus.backend.entites;

import com.mycampus.backend.enumerations.EtatStructure;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"filieres", "professeurs", "chefDepartement"})
@EqualsAndHashCode(exclude = {"filieres", "professeurs", "chefDepartement"})
public class Departement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String codeDepartement;
    
    @Column(nullable = false)
    private String typeDepartement;
    
    private String description;
    
    private LocalDate dateCreation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtatStructure etat;
    
    @OneToMany(mappedBy = "departement")
    private List<Filiere> filieres;

    @OneToMany(mappedBy = "departementOuTravail")
    private List<Professeur> professeurs;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
            name = "chef_departement_id",
            nullable = true,
            unique = true
    )
    private Professeur chefDepartement;
}
