package com.mycampus.backend.entites;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"examen", "etudiant"})
@EqualsAndHashCode(exclude = {"examen", "examen"})
@Table(
        name = "resultat",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_resultat_examen_etudiant",
                columnNames = {"examen_id", "etudiant_id"}
        )
)
public class Resultat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Double note;
    


    @Column(nullable = false)
    private LocalDateTime datePublication;
    
    @Column(nullable = false)
    private Boolean valide = false;
    
    private String copieEtudiantPath;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "examen_id", nullable = false)
    private Examen examen;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;
}
