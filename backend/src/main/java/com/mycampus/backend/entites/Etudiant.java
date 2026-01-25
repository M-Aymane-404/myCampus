package com.mycampus.backend.entites;

import com.mycampus.backend.enumerations.Semestre;
import jakarta.persistence.*;
import lombok.*;
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
@ToString(exclude = {"classe", "presences", "resultats"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)

public class Etudiant extends Utilisateur {
    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true, updatable = false)
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    private Classe classe;
    
    @OneToMany(mappedBy = "etudiant")
    private List<Presence> presences;

    @OneToMany(mappedBy = "etudiant")
    private List<Resultat> resultats;
}
