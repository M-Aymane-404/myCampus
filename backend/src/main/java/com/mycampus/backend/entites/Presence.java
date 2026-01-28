package com.mycampus.backend.entites;

import com.mycampus.backend.enumerations.StatusAbsence;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"seance", "etudiant"})
@EqualsAndHashCode(exclude = {"seance", "etudiant"})
public class Presence {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAbsence status;
    
    @Column(nullable = false)
    private Boolean justifiee = false;
    
    private String motif;
    
    @Column(nullable = false)
    private LocalDateTime dateDeclaration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seance_id", nullable = false)
    private Seance seance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;
    

}
