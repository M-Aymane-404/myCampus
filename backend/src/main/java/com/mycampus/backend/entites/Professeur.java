package com.mycampus.backend.entites;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"affectationEnseignements", "departementOuTravail", "departementDirigee", "filiereDirigee"})
@EqualsAndHashCode(exclude = {"affectationEnseignements", "departementOuTravail", "departementDirigee", "filiereDirigee"})
public class Professeur extends Utilisateur {
    
    @Column(nullable = false, unique = true)
    private String ppr;
    
    @Column(nullable = false)
    private String specialite;
    
    @Column(nullable = false)
    private String grade;

    private LocalDate dateRecrutement;
    
    private String statut;
    
    @OneToMany(mappedBy = "professeur", fetch = FetchType.LAZY)
    private List<AffectationEnseignement> affectationEnseignements;

    @ManyToOne(fetch = FetchType.LAZY)
    private Departement departementOuTravail;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
            name = "departement_dirigee_id",
            nullable = true,
            unique = true
    )
    private Departement departementDirigee;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "chefFiliere")
    private Filiere filiereDirigee;

}
