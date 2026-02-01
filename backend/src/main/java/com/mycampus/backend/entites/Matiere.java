package com.mycampus.backend.entites;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"module", "affectationEnseignements", "examens"})
@EqualsAndHashCode(exclude = {"module", "affectationEnseignements", "examens"})
public class Matiere {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String codeMatiere;
    
    @Column(nullable = false)
    private String nom;
    
    private String description;
    

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;
    
    @OneToMany(mappedBy = "matiere")
    private List<AffectationEnseignement> affectationEnseignements;
    
    @OneToMany(mappedBy = "matiere")
    private List<Examen> examens;
}
