package com.mycampus.backend.entites;

import com.mycampus.backend.enumerations.Semestre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.mycampus.backend.enumerations.EtatInscription;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Etudiant extends Utilisateur {
    
    @Column(nullable = false, unique = true)
    private String apogee;
    
    @Column(nullable = false)
    private String niveau;
    
    @Column(nullable = false)
    private Integer anneeInscription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Semestre semestreActuel;


    private String anneeFinEtude;
    
    private LocalDate dateNaissance;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtatInscription etatInscription;
    
    @ManyToOne
    @JoinColumn(name = "classe_id", nullable = false)
    private Classe classe;
    
    @OneToMany(mappedBy = "etudiant")
    private List<Presence> presences;
    
    @OneToMany(mappedBy = "etudiant")
    private List<Resultat> resultats;
}
