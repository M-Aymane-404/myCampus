package com.mycampus.backend.config;

import com.mycampus.backend.entites.*;
import com.mycampus.backend.enumerations.*;
import com.mycampus.backend.repositorys.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AcademicDataSeeder {
/*
    private final AffectationEnseignementRepository affectationEnseignementRepository;
    private final SeanceRepository seanceRepository;
    private final FichierSeanceRepository fichierSeanceRepository;
    private final PresenceRepository presenceRepository;
    private final ExamenRepository examenRepository;
    private final ResultatRepository resultatRepository;
    private final NotificationRepository notificationRepository;
    private final ClasseRepository classeRepository;
    private final MatiereRepository matiereRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Bean
    public ApplicationRunner academicDataSeederRunner() {
        return args -> {
            log.info("Starting academic data seeding...");
            seedAcademicData();
            log.info("Academic data seeding completed successfully");
        };
    }

    @Transactional
    public void seedAcademicData() {
        seedAffectationEnseignement();
        seedSeances();
        seedFichierSeance();
        seedPresences();
        seedExamens();
        seedResultats();
        seedNotifications();
    }

    private void seedAffectationEnseignement() {
        Classe isi2a = classeRepository.findByNom("ISI2-A").orElse(null);
        Classe isi2b = classeRepository.findByNom("ISI2-B").orElse(null);
        Classe gl2a = classeRepository.findByNom("GL2-A").orElse(null);
        Classe gind2a = classeRepository.findByNom("GIND2-A").orElse(null);
        
        Matiere matiere40 = matiereRepository.findById(40L).orElse(null); // LEAN
        Matiere matiere41 = matiereRepository.findById(41L).orElse(null); // SIX_SIGMA
        Matiere matiere42 = matiereRepository.findById(42L).orElse(null); // ISO_9001
        Matiere matiere43 = matiereRepository.findById(43L).orElse(null); // AUDIT_QUAL
        Matiere matiere48 = matiereRepository.findById(48L).orElse(null); // HTML_CSS
        Matiere matiere49 = matiereRepository.findById(49L).orElse(null); // JAVASCRIPT
        
        Professeur prof14 = (Professeur) utilisateurRepository.findByEmail("prof.web@mycampus.ma").orElse(null);
        Professeur prof13 = (Professeur) utilisateurRepository.findByEmail("prof.gl@mycampus.ma").orElse(null);
        Professeur prof15 = (Professeur) utilisateurRepository.findByEmail("prof.reseau@mycampus.ma").orElse(null);
        
        List<AffectationEnseignement> affectations = Arrays.asList(
            AffectationEnseignement.builder()
                .classe(isi2a).matiere(matiere40).professeur(prof14)
                .dateCreation(LocalDateTime.now()).build(),
            AffectationEnseignement.builder()
                .classe(isi2a).matiere(matiere41).professeur(prof14)
                .dateCreation(LocalDateTime.now()).build(),
            AffectationEnseignement.builder()
                .classe(isi2a).matiere(matiere42).professeur(prof13)
                .dateCreation(LocalDateTime.now()).build(),
            AffectationEnseignement.builder()
                .classe(isi2a).matiere(matiere43).professeur(prof13)
                .dateCreation(LocalDateTime.now()).build(),
            AffectationEnseignement.builder()
                .classe(isi2a).matiere(matiere48).professeur(prof15)
                .dateCreation(LocalDateTime.now()).build(),
            AffectationEnseignement.builder()
                .classe(isi2a).matiere(matiere49).professeur(prof15)
                .dateCreation(LocalDateTime.now()).build()
        );
        
        for (AffectationEnseignement affectation : affectations) {
            if (!affectationEnseignementRepository.existsByClasseAndMatiereAndProfesseur(
                    affectation.getClasse(), affectation.getMatiere(), affectation.getProfesseur())) {
                affectationEnseignementRepository.save(affectation);
                log.info("Created teaching assignment: {} - {} - {}", 
                    affectation.getClasse().getNom(), 
                    affectation.getMatiere().getCode(),
                    affectation.getProfesseur().getEmail());
            }
        }
    }

    private void seedSeances() {
        Matiere matiere48 = matiereRepository.findById(48L).orElse(null); // HTML_CSS
        Matiere matiere49 = matiereRepository.findById(49L).orElse(null); // JAVASCRIPT
        Professeur prof15 = (Professeur) utilisateurRepository.findByEmail("prof.reseau@mycampus.ma").orElse(null);
        Classe isi2a = classeRepository.findByNom("ISI2-A").orElse(null);
        
        List<Seance> seances = Arrays.asList(
            Seance.builder()
                .dateSeance(LocalDate.of(2024, 10, 15))
                .heureDebut(LocalDateTime.of(2024, 10, 15, 8, 0))
                .heureFin(LocalDateTime.of(2024, 10, 15, 10, 0))
                .typeSeance(TypeSeance.COURS)
                .salle("Salle A101")
                .matiere(matiere48)
                .professeur(prof15)
                .classe(isi2a)
                .dateCreation(LocalDateTime.now())
                .build(),
            Seance.builder()
                .dateSeance(LocalDate.of(2024, 10, 17))
                .heureDebut(LocalDateTime.of(2024, 10, 17, 14, 0))
                .heureFin(LocalDateTime.of(2024, 10, 17, 16, 0))
                .typeSeance(TypeSeance.TD)
                .salle("Salle B205")
                .matiere(matiere49)
                .professeur(prof15)
                .classe(isi2a)
                .dateCreation(LocalDateTime.now())
                .build()
        );
        
        for (Seance seance : seances) {
            if (!seanceRepository.existsByDateSeanceAndHeureDebutAndMatiere(
                    seance.getDateSeance(), seance.getHeureDebut(), seance.getMatiere())) {
                seanceRepository.save(seance);
                log.info("Created seance: {} - {} - {}", 
                    seance.getMatiere().getCode(),
                    seance.getDateSeance(),
                    seance.getTypeSeance());
            }
        }
    }

    private void seedFichierSeance() {
        Seance seance1 = seanceRepository.findAll().stream().findFirst().orElse(null);
        
        if (seance1 != null) {
            List<FichierSeance> fichiers = Arrays.asList(
                FichierSeance.builder()
                    .nomFichier("cours_html_css.pdf")
                    .cheminFichier("/files/cours_html_css.pdf")
                    .typeFichier("application/pdf")
                    .tailleFichier(2048000L)
                    .dateUpload(LocalDateTime.now())
                    .seance(seance1)
                    .build(),
                FichierSeance.builder()
                    .nomFichier("exercices_html_css.zip")
                    .cheminFichier("/files/exercices_html_css.zip")
                    .typeFichier("application/zip")
                    .tailleFichier(1024000L)
                    .dateUpload(LocalDateTime.now())
                    .seance(seance1)
                    .build()
            );
            
            for (FichierSeance fichier : fichiers) {
                if (!fichierSeanceRepository.existsByNomFichierAndSeance(
                        fichier.getNomFichier(), fichier.getSeance())) {
                    fichierSeanceRepository.save(fichier);
                    log.info("Created file: {} for seance {}", fichier.getNomFichier(), seance1.getId());
                }
            }
        }
    }

    private void seedPresences() {
        Seance seance1 = seanceRepository.findAll().stream().findFirst().orElse(null);
        
        if (seance1 != null) {
            List<Etudiant> etudiants = utilisateurRepository.findAll().stream()
                .filter(u -> u instanceof Etudiant)
                .map(u -> (Etudiant) u)
                .limit(5)
                .toList();
            
            for (int i = 0; i < etudiants.size(); i++) {
                Etudiant etudiant = etudiants.get(i);
                StatusAbsence status = (i % 2 == 0) ? StatusAbsence.PRESENT : StatusAbsence.ABSENT_JUSTIFIE;
                
                Presence presence = Presence.builder()
                    .datePresence(seance1.getDateSeance())
                    .statusAbsence(status)
                    .seance(seance1)
                    .etudiant(etudiant)
                    .dateCreation(LocalDateTime.now())
                    .build();
                
                if (!presenceRepository.existsByEtudiantAndSeance(etudiant, seance1)) {
                    presenceRepository.save(presence);
                    log.info("Created presence: {} - {} - {}", 
                        etudiant.getEmail(),
                        seance1.getDateSeance(),
                        status);
                }
            }
        }
    }

    private void seedExamens() {
        Matiere matiere48 = matiereRepository.findById(48L).orElse(null); // HTML_CSS
        Classe isi2a = classeRepository.findByNom("ISI2-A").orElse(null);
        
        List<Examen> examens = Arrays.asList(
            Examen.builder()
                .code("HTML_CSS_NORMAL_2024")
                .typeExamen(TypeExamen.NORMAL)
                .dateExamen(LocalDate.of(2024, 12, 10))
                .heureDebut(LocalDateTime.of(2024, 12, 10, 9, 0))
                .duree(120)
                .salle("Salle Exam 1")
                .matiere(matiere48)
                .classe(isi2a)
                .dateCreation(LocalDateTime.now())
                .build(),
            Examen.builder()
                .code("HTML_CSS_RATTRAPAGE_2024")
                .typeExamen(TypeExamen.RATTRAPAGE)
                .dateExamen(LocalDate.of(2025, 1, 15))
                .heureDebut(LocalDateTime.of(2025, 1, 15, 14, 0))
                .duree(120)
                .salle("Salle Exam 2")
                .matiere(matiere48)
                .classe(isi2a)
                .dateCreation(LocalDateTime.now())
                .build()
        );
        
        for (Examen examen : examens) {
            if (!examenRepository.existsByCodeAndTypeExamen(
                    examen.getCode(), examen.getTypeExamen())) {
                examenRepository.save(examen);
                log.info("Created exam: {} - {} - {}", 
                    examen.getCode(),
                    examen.getTypeExamen(),
                    examen.getDateExamen());
            }
        }
    }

    private void seedResultats() {
        Examen examen = examenRepository.findAll().stream().findFirst().orElse(null);
        
        if (examen != null) {
            List<Etudiant> etudiants = utilisateurRepository.findAll().stream()
                .filter(u -> u instanceof Etudiant)
                .map(u -> (Etudiant) u)
                .limit(5)
                .toList();
            
            for (int i = 0; i < etudiants.size(); i++) {
                Etudiant etudiant = etudiants.get(i);
                double note = 10.0 + (Math.random() * 10); // Notes entre 10 et 20
                
                Resultat resultat = Resultat.builder()
                    .note(note)
                    .cheminCopie("/copies/copie_" + etudiant.getId() + ".pdf")
                    .examen(examen)
                    .etudiant(etudiant)
                    .dateCreation(LocalDateTime.now())
                    .build();
                
                if (!resultatRepository.existsByEtudiantAndExamen(etudiant, examen)) {
                    resultatRepository.save(resultat);
                    log.info("Created result: {} - {} - {}", 
                        etudiant.getEmail(),
                        examen.getCode(),
                        note);
                }
            }
        }
    }

    private void seedNotifications() {
        List<Utilisateur> utilisateurs = utilisateurRepository.findAll().stream()
            .limit(3)
            .toList();
        
        List<Notification> notifications = Arrays.asList(
            Notification.builder()
                .titre("Nouveau cours disponible")
                .message("Le cours de HTML/CSS est maintenant disponible en ligne.")
                .dateCreation(LocalDateTime.now())
                .lu(false)
                .utilisateur(utilisateurs.get(0))
                .build(),
            Notification.builder()
                .titre("Rappel examen")
                .message("N'oubliez pas l'examen de HTML/CSS prévu pour le 10 décembre.")
                .dateCreation(LocalDateTime.now())
                .lu(false)
                .utilisateur(utilisateurs.get(1))
                .build(),
            Notification.builder()
                .titre("Note publiée")
                .message("Votre note pour l'examen de JavaScript est maintenant disponible.")
                .dateCreation(LocalDateTime.now())
                .lu(true)
                .utilisateur(utilisateurs.get(2))
                .build()
        );
        
        for (Notification notification : notifications) {
            if (!notificationRepository.existsByTitreAndUtilisateur(
                    notification.getTitre(), notification.getUtilisateur())) {
                notificationRepository.save(notification);
                log.info("Created notification: {} for {}", 
                    notification.getTitre(),
                    notification.getUtilisateur().getEmail());
            }
        }
    }*/
}
