package com.mycampus.backend.entites;

import com.mycampus.backend.enumerations.TypeSeance;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeSeance type;
    
    @Column(nullable = false)
    private Date date;
    
    @Column(nullable = false)
    private String heureDebut;
    
    @Column(nullable = false)
    private String heureFin;
    
    private String salle;
    
    private String etat;
    
    private String contenu;
    
    @ManyToOne
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;
    
    @ManyToOne
    @JoinColumn(name = "professeur_id", nullable = false)
    private Professeur professeur;
    
    @OneToMany(mappedBy = "seance")
    private List<Absence> absences;
    
    @OneToMany(mappedBy = "seance")
    private List<FichierSeance> fichierSeances;
}
