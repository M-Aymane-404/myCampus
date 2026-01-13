package com.mycampus.backend.entites;

import jakarta.persistence.*;
import lombok.*;
import com.mycampus.backend.enumerations.Semestre;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"filiere", "matieres"})
@EqualsAndHashCode(exclude = {"filiere", "matieres"})
public class Module {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String codeModule;
    
    @Column(nullable = false)
    private String nom;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Semestre semestre;
    
    @Column(nullable = false)
    private Integer coefficient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filiere_id", nullable = false)
    private Filiere filiere;
    
    @OneToMany(mappedBy = "module")
    private List<Matiere> matieres;
}
