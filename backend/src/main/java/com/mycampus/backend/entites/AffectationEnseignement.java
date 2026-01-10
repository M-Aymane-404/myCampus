package com.mycampus.backend.entites;

import com.mycampus.backend.enumerations.TypeSeance;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffectationEnseignement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String anneeUniversitaire;
    
    @Column(nullable = false)
    private String semestre;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeSeance typeSeance;
    
    @Column(nullable = false)
    private Boolean active;
    
    @ManyToOne
    @JoinColumn(name = "professeur_id", nullable = false)
    private Professeur professeur;
    
    @ManyToOne
    @JoinColumn(name = "classe_id", nullable = false)
    private Classe classe;
    
    @ManyToOne
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;
    
    @OneToMany(mappedBy = "affectationEnseignement")
    private java.util.List<Seance> seances;
}
