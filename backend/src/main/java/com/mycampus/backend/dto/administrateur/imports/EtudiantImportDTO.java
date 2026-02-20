package com.mycampus.backend.dto.administrateur.imports;


import com.mycampus.backend.enumerations.EtatInscription;
import com.mycampus.backend.enumerations.NiveauAnnee;
import com.mycampus.backend.enumerations.Semestre;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EtudiantImportDTO {
    
    private String apogee;
    private String cin;
    private String nom;
    private String prenom;
    private String email;
    private String tel;
    private NiveauAnnee niveau;
    private Integer anneeInscription;
    private Semestre semestreActuel;
    private EtatInscription etatInscription;
    private LocalDate dateNaissance;
    
    private int numeroLigne;
}
