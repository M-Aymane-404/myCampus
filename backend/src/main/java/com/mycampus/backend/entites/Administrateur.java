package com.mycampus.backend.entites;

import jakarta.persistence.*;
import lombok.*;

@Entity
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)

public class Administrateur extends Utilisateur {


    @Column(nullable = false, unique = true, updatable = false)
    private String ppr;
    
    @Column(nullable = false)
    private String grade;
    
    private String fonction;
}
