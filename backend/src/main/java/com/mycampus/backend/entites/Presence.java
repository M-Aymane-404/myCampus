package com.mycampus.backend.entites;

import com.mycampus.backend.enumerations.StatusAbsence;
import com.mycampus.backend.enumerations.TypeMotif;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Builder
@ToString(exclude = {"seance", "etudiant"})
@EqualsAndHashCode(exclude = {"seance", "etudiant"})
public class Presence {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAbsence status;


    private String descriptionDeJustification;

    @Builder.Default
    private boolean validationMotif = false;

    @Enumerated(EnumType.STRING)
    private TypeMotif typeMotif;
    
    private String motifUrl;

    private String motifNom;


    @Column(nullable = false)
    private LocalDateTime dateDeclaration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seance_id", nullable = false)
    private Seance seance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;
    

}
