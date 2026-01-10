package com.mycampus.backend.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Professeur extends Utilisateur {
    
    @Column(nullable = false, unique = true)
    private String ppr;
    
    @Column(nullable = false)
    private String specialite;
    
    @Column(nullable = false)
    private String grade;

    private LocalDate dateRecrutement;
    
    private String statut;
    
    @OneToMany(mappedBy = "professeur")
    private List<AffectationEnseignement> affectationEnseignements;

    @ManyToOne
    private Departement departementOuTravail;

    @OneToOne(optional = true)
    @JoinColumn(
            name = "departement_dirigee_id",
            nullable = true,
            unique = true
    )
    private Departement departementDirigee;

    @OneToOne(mappedBy = "chefFiliere")
    private Filiere filiereDirigee;

}
