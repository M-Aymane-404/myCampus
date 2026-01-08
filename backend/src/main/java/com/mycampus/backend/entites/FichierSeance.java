package com.mycampus.backend.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FichierSeance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nomOriginal;
    
    @Column(nullable = false)
    private String chemin;
    
    @Column(nullable = false)
    private String typeMime;
    
    @Column(nullable = false)
    private Long taille;
    
    @Column(nullable = false)
    private Date dateUpload;
    
    @ManyToOne
    @JoinColumn(name = "seance_id", nullable = false)
    private Seance seance;
}
