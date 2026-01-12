package com.mycampus.backend.entites;

import com.mycampus.backend.enumerations.EtatSeance;
import com.mycampus.backend.enumerations.TypeSeance;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"affectationEnseignement", "presences", "fichierSeances"})
@EqualsAndHashCode(exclude = {"affectationEnseignement", "presences", "fichierSeances"})
public class Seance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeSeance type;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private LocalDateTime heureDebut;
    
    @Column(nullable = false)
    private LocalDateTime heureFin;
    
    private String salle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtatSeance etat;
    
    private String contenu;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affectation_enseignement_id", nullable = false)
    private AffectationEnseignement affectationEnseignement;
    
    @OneToMany(mappedBy = "seance", fetch = FetchType.LAZY)
    private List<Presence> presences;
    
    @OneToMany(mappedBy = "seance", fetch = FetchType.LAZY)
    private List<FichierSeance> fichierSeances;
}
