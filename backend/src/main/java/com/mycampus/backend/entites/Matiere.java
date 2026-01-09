package com.mycampus.backend.entites;

import com.mycampus.backend.enumerations.TypeSeance;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Matiere {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String codeMatiere;
    
    @Column(nullable = false)
    private String nom;
    
    private String description;
    
    @Column(nullable = false)
    private Integer coefficient;
    
    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;
    
    @OneToMany(mappedBy = "matiere")
    private List<Seance> seances;
    
    @OneToMany(mappedBy = "matiere")
    private List<Examen> examens;
}
