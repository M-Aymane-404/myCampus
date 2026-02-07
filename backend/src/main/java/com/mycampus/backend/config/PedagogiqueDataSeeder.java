package com.mycampus.backend.config;

import com.mycampus.backend.entites.*;
import com.mycampus.backend.entites.Module;
import com.mycampus.backend.enumerations.*;
import com.mycampus.backend.repositorys.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PedagogiqueDataSeeder {

    private final ModuleRepository moduleRepository;
    private final MatiereRepository matiereRepository;
    private final AffectationEnseignementRepository affectationEnseignementRepository;
    private final SeanceRepository seanceRepository;
    private final FichierSeanceRepository fichierSeanceRepository;
    private final PresenceRepository presenceRepository;
    private final ExamenRepository examenRepository;
    private final ResultatRepository resultatRepository;
    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final FiliereRepository filiereRepository;
    private final ClasseRepository classeRepository;



    @Transactional
    public void seedPedagogicalData() {
        validatePrerequisites();
        
        seedModules();
        seedMatieres();
        seedAffectationEnseignement();
        seedSeances();
        seedFichiersSeance();
        seedPresences();
        seedExamens();
        seedResultats();
        seedNotifications();
    }

    private void validatePrerequisites() {
        requireProfesseur("prof.web@mycampus.ma");
        requireProfesseur("prof.gl@mycampus.ma");

        requireClasse("ILCS1-A");
        requireClasse("GIND1-A");


        requireFiliere("GIND");
        requireFiliere("ILCS");
         log.info("All prerequisites validated for pedagogical seeding");
    }

    private void seedModules() {
        Filiere ilcs = requireFiliere("ILCS");
         Filiere gind = requireFiliere("GIND");
        
        List<Module> modules = Arrays.asList(

            Module.builder()
                .codeModule("ISI-RESEAU-S3")
                .nom("Réseaux S3")
                .description("Réseaux informatiques et protocoles")
                .semestre(Semestre.S3)
                .coefficient(3)
                .filiere(ilcs)
                .build(),


            Module.builder()
                .codeModule("GIND-PROD-S1")
                .nom("Production Industrielle S1")
                .description("Optimisation des processus de production")
                .semestre(Semestre.S1)
                .coefficient(4)
                .filiere(gind)
                .build()
        );
        
        for (Module module : modules) {
            if (!moduleRepository.existsByCodeModule(module.getCodeModule())) {
                moduleRepository.save(module);
                log.info("Created module: {}", module.getCodeModule());
            }
        }
    }

    private void seedMatieres() {

        Module isiReseau = requireModule("ISI-RESEAU-S3");

        Module gindProd = requireModule("GIND-PROD-S1");

        
        List<Matiere> matieres = Arrays.asList(

            Matiere.builder()
                .codeMatiere("RESEAUX")
                .nom("Réseaux")
                .description("Protocoles réseau et architecture TCP/IP")
                .module(isiReseau)
                .build(),


            Matiere.builder()
                .codeMatiere("LEAN")
                .nom("Lean Manufacturing")
                .description("Principes du Lean Manufacturing")
                .module(gindProd)
                .build(),

            Matiere.builder()
                .codeMatiere("ISO_9001")
                .nom("ISO 9001")
                .description("Norme ISO 9001 qualité")
                .module(isiReseau)
                .build()

        );
        
        for (Matiere matiere : matieres) {
            if (!matiereRepository.existsByCodeMatiere(matiere.getCodeMatiere())) {
                matiereRepository.save(matiere);
                log.info("Created matiere: {}", matiere.getCodeMatiere());
            }
        }
    }

    private void seedAffectationEnseignement() {
        Professeur profWeb = requireProfesseur("prof.web@mycampus.ma");
        Professeur profGl = requireProfesseur("prof.gl@mycampus.ma");

        Classe gind = requireClasse("GIND1-A");
        Classe ilcs = requireClasse("ILCS1-A");

        

        Matiere reseaux = requireMatiere("RESEAUX");

        Matiere lean = requireMatiere("LEAN");

        Matiere iso9001 = requireMatiere("ISO_9001");

        
        List<AffectationEnseignement> affectations = Arrays.asList(
            AffectationEnseignement.builder()
                .professeur(profWeb).classe(gind).matiere(lean)
                .active(true)
                .build(),
            AffectationEnseignement.builder()
                .professeur(profWeb).classe(gind).matiere(iso9001)
                .active(true)
                .build(),


            AffectationEnseignement.builder()
                .professeur(profGl).classe(ilcs).matiere(reseaux)
                .active(true)
                .build()


        );
        
        for (AffectationEnseignement affectation : affectations) {
            if (!affectationEnseignementRepository.existsByClasseAndMatiereAndProfesseur(
                    affectation.getClasse(), affectation.getMatiere(), affectation.getProfesseur())) {
                affectationEnseignementRepository.save(affectation);
                log.info("Created affectation: {} - {} - {}", 
                    affectation.getProfesseur().getEmail(),
                    affectation.getClasse().getCodeClasse(),
                    affectation.getMatiere().getCodeMatiere());
            }
        }
    }

    private void seedSeances() {
        List<AffectationEnseignement> affectations = affectationEnseignementRepository.findByActiveTrue();
        
        for (AffectationEnseignement affectation : affectations) {
            LocalDate today = LocalDate.now();
            
            for (int i = 0; i < 2; i++) {
                LocalDate dateSeance = today.plusWeeks(i);
                LocalDateTime heureDebut = LocalDateTime.now();
                LocalDateTime heureFin = LocalDateTime.now();
                

                    
                    Seance seance = Seance.builder()
                        .type(TypeSeance.COURS)
                        .date(dateSeance)
                        .heureDebut(heureDebut)
                        .heureFin(heureFin)
                        .salle("Salle " + (i + 1))
                        .etat(EtatSeance.PLANIFIEE)
                        .contenu("Contenu de séance pour " + affectation.getMatiere().getNom())
                        .affectationEnseignement(affectation)
                        .build();
                    
                    seanceRepository.save(seance);
                    log.info("Created seance: {} - {} - {}", 
                        affectation.getMatiere().getCodeMatiere(),
                        dateSeance,
                        heureDebut);

            }
        }
    }

    private void seedFichiersSeance() {
        List<Seance> seances = seanceRepository.findAll();
        
        for (Seance seance : seances) {
            FichierSeance fichier = FichierSeance.builder()
                .nomOriginal("cours_" + seance.getType().name() + ".pdf")
                .chemin("/fichiers/cours_" + seance.getId() + ".pdf")
                .typeMime("application/pdf")
                .taille(1024000L)
                .dateUpload(LocalDateTime.now())
                .seance(seance)
                .build();
            
            fichierSeanceRepository.save(fichier);


            FichierSeance fichier1 = FichierSeance.builder()
                    .nomOriginal("TD_" + seance.getType().name() + ".pdf")
                    .chemin("/fichiers/TD_" + seance.getId() + ".pdf")
                    .typeMime("application/pdf")
                    .taille(1025000L)
                    .dateUpload(LocalDateTime.now())
                    .seance(seance)
                    .build();

            fichierSeanceRepository.save(fichier1);
            log.info("Created fichier for seance: {}", seance.getId());
        }
    }

    private void seedPresences() {
        List<Seance> seances = seanceRepository.findAll();
        List<Etudiant> etudiants = utilisateurRepository.findAll().stream()
            .filter(u -> u instanceof Etudiant)
            .map(u -> (Etudiant) u)
            .limit(10)
            .toList();
        
        for (Seance seance : seances) {
            for (Etudiant etudiant : etudiants) {
                if (!presenceRepository.existsBySeanceAndEtudiant(seance, etudiant)) {
                    StatusAbsence status = Math.random() > 0.1 ? StatusAbsence.PRESENT : StatusAbsence.ABSENT;
                    
                    Presence presence = Presence.builder()
                        .status(status)
                        .justifiee(status == StatusAbsence.ABSENT && Math.random() > 0.5)
                        .motif(status == StatusAbsence.ABSENT ? "Retard" : null)
                        .dateDeclaration(LocalDateTime.now())
                        .seance(seance)
                        .etudiant(etudiant)
                        .build();
                    
                    presenceRepository.save(presence);
                    log.info("Created presence: {} - {} - {}", 
                        etudiant.getEmail(),
                        seance.getDate(),
                        status);
                }
            }
        }
    }

    private void seedExamens() {
        Matiere reseaux = requireMatiere("RESEAUX");
        Matiere iso = requireMatiere("ISO_9001");
        Matiere lean = requireMatiere("LEAN");
        
        List<Examen> examens = Arrays.asList(
            Examen.builder()
                .semestre(Semestre.S3)
                .dateExamen(Date.from(LocalDateTime.of(2024, 12, 10, 9, 0)
                    .atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .numeroExamen(1)
                .typeExamen(TypeExamen.NORMAL)
                .duree(120)
                .etat(EtatExamen.PROGRAMME)
                .corrigePath("/corriges/html_css_normal_corrige.pdf")
                .matiere(reseaux)
                .build(),
            Examen.builder()
                .semestre(Semestre.S3)
                .dateExamen(Date.from(LocalDateTime.of(2024, 12, 12, 14, 0)
                    .atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .numeroExamen(1)
                .typeExamen(TypeExamen.NORMAL)
                .duree(120)
                .etat(EtatExamen.PROGRAMME)
                .corrigePath("/corriges/javascript_normal_corrige.pdf")
                .matiere(iso)
                .build(),

            Examen.builder()
                .semestre(Semestre.S1)
                .dateExamen(Date.from(LocalDateTime.of(2024, 11, 20, 10, 0)
                    .atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .numeroExamen(1)
                .typeExamen(TypeExamen.NORMAL)
                .duree(90)
                .etat(EtatExamen.PASSE)
                .corrigePath("/corriges/lean_normal_corrige.pdf")
                .matiere(lean)
                .build()
        );
        
        for (Examen examen : examens) {
            examenRepository.save(examen);
            log.info("Created exam: {} - {} - {}", 
                examen.getMatiere().getCodeMatiere(),
                examen.getTypeExamen(),
                examen.getDateExamen());
        }
    }

    private void seedResultats() {
        List<Examen> examens = examenRepository.findAll();
        List<Etudiant> etudiants = utilisateurRepository.findAll().stream()
            .filter(u -> u instanceof Etudiant)
            .map(u -> (Etudiant) u)
            .limit(10)
            .toList();
        
        for (Examen examen : examens) {
            for (Etudiant etudiant : etudiants) {
                if (!resultatRepository.existsByExamenAndEtudiant(examen, etudiant)) {
                    double note = 8.0 + (Math.random() * 12.0);
                    
                    Resultat resultat = Resultat.builder()
                        .note(Math.round(note * 100.0) / 100.0)
                        .datePublication(LocalDateTime.now())
                        .copieEtudiantPath("/copies/copie_" + etudiant.getId() + "_" + examen.getId() + ".pdf")
                        .examen(examen)
                        .etudiant(etudiant)
                        .build();
                    
                    resultatRepository.save(resultat);
                    log.info("Created result: {} - {} - {}", 
                        etudiant.getEmail(),
                        examen.getMatiere().getCodeMatiere(),
                        resultat.getNote());
                }
            }
        }
    }

    private void seedNotifications() {
        List<Utilisateur> utilisateurs = utilisateurRepository.findAll().stream()
            .limit(5)
            .toList();
        
        List<Notification> notifications = Arrays.asList(
            Notification.builder()
                .titre("Nouveau cours disponible")
                .message("Le cours de HTML/CSS est maintenant disponible en ligne.")
                .dateEnvoi(LocalDateTime.now())
                .type(TypeNotification.COURS_UPLOADE)
                .utilisateur(utilisateurs.get(0))
                .build(),
            Notification.builder()
                .titre("Rappel examen")
                .message("N'oubliez pas l'examen de HTML/CSS prévu pour le 10 décembre.")
                .dateEnvoi(LocalDateTime.now())
                .type(TypeNotification.EXAMEN_DEMAIN)
                .utilisateur(utilisateurs.get(1))
                .build(),
            Notification.builder()
                .titre("Note publiée")
                .message("Votre note pour l'examen de JavaScript est maintenant disponible.")
                .dateEnvoi(LocalDateTime.now())
                .lu(true)
                .dateLecture(LocalDateTime.now().minusHours(2))
                .type(TypeNotification.RESULTAT_PUBLIE)
                .utilisateur(utilisateurs.get(2))
                .build(),
            Notification.builder()
                .titre("Séance ajoutée")
                .message("Une nouvelle séance de TD a été ajoutée à votre emploi du temps.")
                .dateEnvoi(LocalDateTime.now().minusDays(1))
                .lu(true)
                .dateLecture(LocalDateTime.now().minusHours(12))
                .type(TypeNotification.SEANCE_AJOUTEE)
                .utilisateur(utilisateurs.get(3))
                .build(),
            Notification.builder()
                .titre("Résultat publié")
                .message("Les résultats de l'examen de Lean Manufacturing sont publiés.")
                .dateEnvoi(LocalDateTime.now().minusDays(2))
                .type(TypeNotification.RESULTAT_PUBLIE)
                .utilisateur(utilisateurs.get(4))
                .build()
        );
        
        for (Notification notification : notifications) {
            if (!notificationRepository.existsByUtilisateurAndTypeAndDateEnvoi(
                    notification.getUtilisateur(), 
                    notification.getType(), 
                    notification.getDateEnvoi())) {
                notificationRepository.save(notification);
                log.info("Created notification: {} for {}", 
                    notification.getTitre(),
                    notification.getUtilisateur().getEmail());
            }
        }
    }

    private Professeur requireProfesseur(String email) {
        return (Professeur) utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Professeur " + email + " not found during pedagogical seeding"));
    }

    private Filiere requireFiliere(String codeFiliere) {
        return filiereRepository.findByCodeFiliere(codeFiliere)
                .orElseThrow(() -> new RuntimeException("Filiere " + codeFiliere + " not found during pedagogical seeding"));
    }

    private Classe requireClasse(String codeClasse) {
        return classeRepository.findByCodeClasse(codeClasse)
                .orElseThrow(() -> new RuntimeException("Classe " + codeClasse + " not found during pedagogical seeding"));
    }

    private Module requireModule(String codeModule) {
        return moduleRepository.findByCodeModule(codeModule)
                .orElseThrow(() -> new RuntimeException("Module " + codeModule + " not found during pedagogical seeding"));
    }

    private Matiere requireMatiere(String codeMatiere) {
        return matiereRepository.findByCodeMatiere(codeMatiere)
                .orElseThrow(() -> new RuntimeException("Matiere " + codeMatiere + " not found during pedagogical seeding"));
    }
}
