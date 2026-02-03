package com.mycampus.backend.entites;

import com.mycampus.backend.enumerations.TypeNotification;
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
@Builder
@ToString(exclude = {"utilisateur"})
@EqualsAndHashCode(exclude = {"utilisateur"})
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String titre;
    
    @Column(nullable = false)
    private String message;
    
    @Column(nullable = false)
    private LocalDateTime dateEnvoi;
    
    @Column(nullable = false)
    private Boolean lu = false;
    
    private LocalDateTime dateLecture;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeNotification type;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;
}
