package com.mycampus.backend.dto.etudiant.DonneeSortie;

import com.mycampus.backend.enumerations.EtatInscription;
import com.mycampus.backend.enumerations.NiveauAnnee;
import com.mycampus.backend.enumerations.Semestre;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtudiantSortieEtudiantDTO {

    private Long id;

    private String nom;

    private String prenom;

    private String email;

    private String cin;

    private String tel;

    private String photoUrl;
    private String photoNom;

    private Boolean active ;

    private String apogee;

    private NiveauAnnee niveau;

    private Integer anneeInscription;

    private Semestre semestreActuel;

    private String anneeFinEtude;

    private LocalDate dateNaissance;

    private EtatInscription etatInscription;

    private String codeClasse;

    private Integer dureeFormation;

    private Integer nombreSemestres;

    private LocalDateTime dateDerniereConnexion;
}
