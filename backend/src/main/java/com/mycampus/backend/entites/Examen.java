package com.mycampus.backend.entites;

import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.TypeExamen;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.mycampus.backend.enumerations.EtatExamen;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Examen {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Semestre semestre;

    
    @Column(nullable = false)
    private Date dateExamen;
    
    @Column(nullable = false)
    private Integer numeroExamen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeExamen typeExamen;
    
    @Column(nullable = false)
    private Integer duree;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtatExamen etat;
    
    private String feuilleCorrection;
    
    @ManyToOne
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;
    
    @OneToMany(mappedBy = "examen")
    private List<Resultat> resultats;
}
