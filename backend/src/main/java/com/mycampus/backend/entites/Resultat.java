package com.mycampus.backend.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Resultat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Double note;
    
    private String mention;
    
    @Column(nullable = false)
    private LocalDateTime datePublication;
    
    @Column(nullable = false)
    private Boolean valide = false;
    
    private String fichierExam;
    
    @ManyToOne
    @JoinColumn(name = "examen_id", nullable = false)
    private Examen examen;
    
    @ManyToOne
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;
}
