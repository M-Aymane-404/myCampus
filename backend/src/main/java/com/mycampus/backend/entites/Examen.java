package com.mycampus.backend.entites;

import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.TypeExamen;
import jakarta.persistence.*;
import lombok.*;
import com.mycampus.backend.enumerations.EtatExamen;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"matiere", "resultats"})
@EqualsAndHashCode(exclude = {"matiere", "resultats"})
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;
    
    @OneToMany(mappedBy = "examen" ,fetch = FetchType.LAZY)
    private List<Resultat> resultats;
}
