package com.mycampus.backend.entites;

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
    
    @OneToMany(mappedBy = "departement")
    private List<Filiere> filieres;

    @OneToMany(mappedBy = "departementOuTravail")
    private List<Professeur> professeurs;

    @OneToOne(mappedBy = "departementDirigee")
    private Professeur chefDepartement;
}
