package com.mycampus.backend.entites;

import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.TypeSeance;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"professeur", "classe", "matiere","seances"})
@EqualsAndHashCode(exclude = {"professeur", "classe", "matiere","seances"})
public class AffectationEnseignement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String anneeUniversitaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Semestre semestre;
    

    
    @Column(nullable = false)
    private Boolean active;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professeur_id", nullable = false)
    private Professeur professeur;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    private Classe classe;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;
    
    @OneToMany(mappedBy = "affectationEnseignement", fetch = FetchType.LAZY)
    private List<Seance> seances;
}
