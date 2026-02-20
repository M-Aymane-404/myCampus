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
    private final AnnonceRepository annonceRepository;
    private final DepartementRepository departementRepository;
    private final EtudiantRepository etudiantRepository;
    private final ProfesseurRepository professeurRepository;
    private final AdministrateurRepository administrateurRepository;



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
      //  seedNotifications();
        seedAnnonce();
    }

    private void validatePrerequisites() {
        requireProfesseur("prof.info1@mycampus.ma");

        requireClasse("ILCS1-A");


         requireFiliere("ILCS");
         log.info("All prerequisites validated for pedagogical seeding");
    }

    private void seedModules() {
        Filiere ilcs = requireFiliere("ILCS");

        List<Module> modules = Arrays.asList(

                Module.builder()
                        .codeModule("ILCS-RESEAU-S1")
                        .nom("Réseaux S1")
                        .description("Réseaux informatiques et protocoles")
                        .semestre(Semestre.S1)

                        .filiere(ilcs)
                        .build(),

                Module.builder()
                        .codeModule("ILCS-POO-S1")
                        .nom("Programmation Orientée Objet")
                        .description("Concepts avancés de la programmation orientée objet")
                        .semestre(Semestre.S1)
                         .filiere(ilcs)
                        .build(),

                Module.builder()
                        .codeModule("ILCS-BDD-S1")
                        .nom("Bases de Données")
                        .description("Modélisation et gestion des bases de données relationnelles")
                        .semestre(Semestre.S1)
                         .filiere(ilcs)
                        .build(),

                Module.builder()
                        .codeModule("ILCS-SYSTEME-S1")
                        .nom("Systèmes d’Exploitation")
                        .description("Fonctionnement des systèmes d’exploitation")
                        .semestre(Semestre.S1)
                         .filiere(ilcs)
                        .build(),

                 Module.builder()
                        .codeModule("ILCS-LANGAGES-ETRANGERE-S1")
                        .nom("Langages  étrangère")
                        .description("Étude des langages étrangère")
                        .semestre(Semestre.S1)
                         .filiere(ilcs)
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

        Filiere ilcs = requireFiliere("ILCS");

// ✅ récupérer les modules ILCS S1
        Module ilcsReseau   = requireModule("ILCS-RESEAU-S1");
        Module ilcsPoo      = requireModule("ILCS-POO-S1");
        Module ilcsBdd      = requireModule("ILCS-BDD-S1");
        Module ilcsSysteme  = requireModule("ILCS-SYSTEME-S1");
        Module ilcsLangues  = requireModule("ILCS-LANGAGES-ETRANGERE-S1");

// ✅ 6 matières
        List<Matiere> matieres = Arrays.asList(

                // 1) Réseaux (même nom/description que le module)
                Matiere.builder()
                        .codeMatiere("ILCS-RESEAU-S1-M1")
                        .nom("Réseaux S1")
                        .description("Réseaux informatiques et protocoles")
                        .module(ilcsReseau)
                        .build(),

                // 2) POO (même nom/description que le module)
                Matiere.builder()
                        .codeMatiere("ILCS-POO-S1-M1")
                        .nom("Programmation Orientée Objet")
                        .description("Concepts avancés de la programmation orientée objet")
                        .module(ilcsPoo)
                        .build(),

                // 3) BDD (même nom/description que le module)
                Matiere.builder()
                        .codeMatiere("ILCS-BDD-S1-M1")
                        .nom("Bases de Données")
                        .description("Modélisation et gestion des bases de données relationnelles")
                        .module(ilcsBdd)
                        .build(),

                // 4) Systèmes (même nom/description que le module)
                Matiere.builder()
                        .codeMatiere("ILCS-SYSTEME-S1-M1")
                        .nom("Systèmes d’Exploitation")
                        .description("Fonctionnement des systèmes d’exploitation")
                        .module(ilcsSysteme)
                        .build(),

                // 5) Langues étrangères - Français
                Matiere.builder()
                        .codeMatiere("ILCS-LANGUES-S1-FR")
                        .nom("Français")
                        .description("Communication écrite et orale en français")
                        .module(ilcsLangues)
                        .build(),

                // 6) Langues étrangères - Anglais
                Matiere.builder()
                        .codeMatiere("ILCS-LANGUES-S1-EN")
                        .nom("Anglais")
                        .description("Communication écrite et orale en anglais")
                        .module(ilcsLangues)
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
        Professeur profInfo1 = requireProfesseur("prof.info1@mycampus.ma");
        Professeur profInfo2 = requireProfesseur("prof.info2@mycampus.ma");

        Classe ilcs1A = requireClasse("ILCS1-A");

// 6 matières (codes selon ce que tu as créé)
        Matiere reseauxS1   = requireMatiere("ILCS-RESEAU-S1-M1");
        Matiere pooS1       = requireMatiere("ILCS-POO-S1-M1");
        Matiere bddS1       = requireMatiere("ILCS-BDD-S1-M1");
        Matiere systemeS1   = requireMatiere("ILCS-SYSTEME-S1-M1");
        Matiere francaisS1  = requireMatiere("ILCS-LANGUES-S1-FR");
        Matiere anglaisS1   = requireMatiere("ILCS-LANGUES-S1-EN");

        List<AffectationEnseignement> affectations = Arrays.asList(

                // ✅ Prof 1 : 3 matières
                AffectationEnseignement.builder()
                        .professeur(profInfo1)
                        .classe(ilcs1A)
                        .matiere(reseauxS1)
                        .anneeUniversitaire("2025-2026")
                        .active(true)
                        .build(),

                AffectationEnseignement.builder()
                        .professeur(profInfo1)
                        .classe(ilcs1A)
                        .matiere(pooS1)
                        .anneeUniversitaire("2025-2026")
                        .active(true)
                        .build(),


                AffectationEnseignement.builder()
                        .professeur(profInfo1)
                        .classe(ilcs1A)
                        .matiere(anglaisS1)
                        .anneeUniversitaire("2025-2026")
                        .active(true)
                        .build(),

                // ✅ Prof 2 : 3 matières
                AffectationEnseignement.builder()
                        .professeur(profInfo2)
                        .classe(ilcs1A)
                        .matiere(systemeS1)
                        .anneeUniversitaire("2025-2026")
                        .active(true)
                        .build(),
                AffectationEnseignement.builder()
                        .professeur(profInfo2)
                        .classe(ilcs1A)
                        .matiere(bddS1)
                        .anneeUniversitaire("2025-2026")
                        .active(true)
                        .build(),

                AffectationEnseignement.builder()
                        .professeur(profInfo2)
                        .classe(ilcs1A)
                        .matiere(francaisS1)
                        .anneeUniversitaire("2025-2026")
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

        LocalDate today = LocalDate.now();

// 3 boucles (3 séries)
        for (int serie = 0; serie < 3; serie++) {

            // base date différente pour chaque série
            LocalDate baseDate = today.plusWeeks(serie);

            for (AffectationEnseignement affectation : affectations) {

                // chaque série crée 4 séances
                for (int i = 0; i < 4; i++) {

                    // date différente dans la même semaine (ex: lundi, mardi, ...)
                    LocalDate dateSeance = baseDate.plusDays(i);

                    // heures différentes
                    LocalDateTime heureDebut = dateSeance.atTime(8 + (i * 2), 0); // 08:00, 10:00, 12:00, 14:00
                    LocalDateTime heureFin   = heureDebut.plusHours(2);

                    // type différent
                    TypeSeance type = switch (i) {
                        case 0 -> TypeSeance.COURS;
                        case 1 -> TypeSeance.TD;
                        case 2 -> TypeSeance.TP;
                        default -> TypeSeance.COURS;
                    };

                    // état différent (par série)
                    EtatSeance etat = switch (serie) {
                        case 0 -> EtatSeance.PLANIFIEE;
                        case 1 -> EtatSeance.ANNULEE;
                        default -> EtatSeance.TERMINEE;
                    };

                    Seance seance = Seance.builder()
                            .type(type)
                            .date(dateSeance)
                            .heureDebut(heureDebut)
                            .heureFin(heureFin)
                            .salle("Salle " + (serie + 1) + "-" + (i + 1))
                            .etat(etat)
                            .contenu("Séance " + type + " (" + etat + ") - " + affectation.getMatiere().getNom())
                            .affectationEnseignement(affectation)
                            .build();

                    seanceRepository.save(seance);

                    log.info("Created seance: {} | {} | {} -> {} | {} | {}",
                            affectation.getMatiere().getCodeMatiere(),
                            type,
                            etat,
                            dateSeance,
                            heureDebut.toLocalTime(),
                            heureFin.toLocalTime()
                    );
                }
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
        Matiere reseauxS1   = requireMatiere("ILCS-RESEAU-S1-M1");
        Matiere pooS1       = requireMatiere("ILCS-POO-S1-M1");
        Matiere bddS1       = requireMatiere("ILCS-BDD-S1-M1");
        Matiere systemeS1   = requireMatiere("ILCS-SYSTEME-S1-M1");
        Matiere francaisS1  = requireMatiere("ILCS-LANGUES-S1-FR");
        Matiere anglaisS1   = requireMatiere("ILCS-LANGUES-S1-EN");
        
        List<Examen> examens = Arrays.asList(
            Examen.builder()
                .semestre(Semestre.S1)
                .dateExamen(Date.from(LocalDateTime.of(2024, 12, 10, 9, 0)
                    .atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .numeroExamen(1)
                .typeExamen(TypeExamen.NORMAL)
                .duree(120)
                .etat(EtatExamen.PROGRAMME)
                .corrigePath("/corriges/html_css_normal_corrige.pdf")
                .matiere(reseauxS1)
                .build(),
            Examen.builder()
                .semestre(Semestre.S1)
                .dateExamen(Date.from(LocalDateTime.of(2024, 12, 11, 14, 0)
                    .atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .numeroExamen(1)
                .typeExamen(TypeExamen.NORMAL)
                .duree(120)
                .etat(EtatExamen.PROGRAMME)
                .corrigePath("/corriges/javascript_normal_corrige.pdf")
                .matiere(systemeS1)
                .build(),

            Examen.builder()
                .semestre(Semestre.S1)
                .dateExamen(Date.from(LocalDateTime.of(2024, 11, 12, 10, 0)
                    .atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .numeroExamen(1)
                .typeExamen(TypeExamen.NORMAL)
                .duree(90)
                .etat(EtatExamen.PASSE)
                .corrigePath("/corriges/lean_normal_corrige.pdf")
                .matiere(bddS1)
                .build(),

                Examen.builder()
                        .semestre(Semestre.S1)
                        .dateExamen(Date.from(LocalDateTime.of(2024, 11, 13, 10, 0)
                                .atZone(java.time.ZoneId.systemDefault()).toInstant()))
                        .numeroExamen(1)
                        .typeExamen(TypeExamen.NORMAL)
                        .duree(90)
                        .etat(EtatExamen.PASSE)
                        .corrigePath("/corriges/lean_normal_corrige.pdf")
                        .matiere(pooS1)
                        .build(),
                Examen.builder()
                        .semestre(Semestre.S1)
                        .dateExamen(Date.from(LocalDateTime.of(2024, 11, 14, 10, 0)
                                .atZone(java.time.ZoneId.systemDefault()).toInstant()))
                        .numeroExamen(1)
                        .typeExamen(TypeExamen.NORMAL)
                        .duree(90)
                        .etat(EtatExamen.PASSE)
                        .corrigePath("/corriges/lean_normal_corrige.pdf")
                        .matiere(francaisS1)
                        .build(),
                Examen.builder()
                        .semestre(Semestre.S1)
                        .dateExamen(Date.from(LocalDateTime.of(2024, 11, 15, 10, 0)
                                .atZone(java.time.ZoneId.systemDefault()).toInstant()))
                        .numeroExamen(1)
                        .typeExamen(TypeExamen.NORMAL)
                        .duree(90)
                        .etat(EtatExamen.PROGRAMME)
                        .corrigePath("/corriges/lean_normal_corrige.pdf")
                        .matiere(anglaisS1)
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
        List<Etudiant> etudiants = etudiantRepository.findAll().stream()
            .limit(5)
            .toList();
        List<Administrateur> admis = administrateurRepository.findAll().stream()
                .limit(5)
                .toList();
        List<Professeur> profs = professeurRepository.findAll().stream()
                .limit(5)
                .toList();

        List<Notification> notifications = Arrays.asList(
            Notification.builder()
                .titre("Nouveau cours disponible")
                .message("Le cours de HTML/CSS est maintenant disponible en ligne.")
                .dateEnvoi(LocalDateTime.now())
                .type(TypeNotification.COURS_UPLOADE)
                .utilisateur(etudiants.get(0))
                    .auteur(profs.get(0))
                .build(),
            Notification.builder()
                .titre("Rappel examen")
                .message("N'oubliez pas l'examen de HTML/CSS prévu pour le 10 décembre.")
                .dateEnvoi(LocalDateTime.now())
                .type(TypeNotification.EXAMEN_DEMAIN)
                .utilisateur(etudiants.get(1))
                .build(),
            Notification.builder()
                .titre("Note publiée")
                .message("Votre note pour l'examen de JavaScript est maintenant disponible.")
                .dateEnvoi(LocalDateTime.now())
                .lu(true)
                .dateLecture(LocalDateTime.now().minusHours(2))
                .type(TypeNotification.RESULTAT_PUBLIE)
                .utilisateur(etudiants.get(2))
                .build(),
            Notification.builder()
                .titre("Séance ajoutée")
                .message("Une nouvelle séance de TD a été ajoutée à votre emploi du temps.")
                .dateEnvoi(LocalDateTime.now().minusDays(1))
                .lu(true)
                .dateLecture(LocalDateTime.now().minusHours(12))
                .type(TypeNotification.SEANCE_AJOUTEE)
                .utilisateur(etudiants.get(3))
                .build(),
            Notification.builder()
                .titre("Résultat publié")
                .message("Les résultats de l'examen de Lean Manufacturing sont publiés.")
                .dateEnvoi(LocalDateTime.now().minusDays(2))
                .type(TypeNotification.RESULTAT_PUBLIE)
                .utilisateur(etudiants.get(4))
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
    public void seedAnnonce(){
       Professeur prof = requireProfesseur("prof.info1@mycampus.ma");
       Classe ilcs = requireClasse("ILCS1-A");
       Administrateur admin = requireAdministrateur("admin1@mycampus.ma");
        Departement infoDept = requireDepartement("INFO");
       LocalDateTime today = LocalDateTime.now();
       annonceRepository.save( Annonce.builder()
                .titre("examen est annullé")
                .contenu("examen est annullé examen est annullé examen est annullé examen est annullé")
                .cibleType(AnnonceCibleType.ETUDIANTS_CLASSE)
                .auteur(prof)
                       .datePublication(today)
                .classe(ilcs)
                .publiee(true)
                .build());

        annonceRepository.save( Annonce.builder()
                .titre("la rentrer scolaire")
                .contenu("la rentrer scolaire le 20/9/2026")
                .cibleType(AnnonceCibleType.ETUDIANTS_GLOBAL)
                .auteur(admin)
                        .datePublication( today.minusDays(2))
                .publiee(true)
                .build());

        annonceRepository.save( Annonce.builder()
                .titre("les prod de info ")
                .contenu("il faut  que les exam doit terminer avant la semaine dernière de mois 4")
                .cibleType(AnnonceCibleType.PROFESSEURS_DEPARTEMENT)
                .auteur(prof)
                .datePublication(today)
                .departement(infoDept)
                .publiee(true)
                .build());

        annonceRepository.save( Annonce.builder()
                .titre("les notes ")
                .contenu("les notees doit etre entrer au site avant le 24/2/2026")
                .cibleType(AnnonceCibleType.PROFESSEURS_GLOBAL)
                .auteur(admin)
                .datePublication( today.minusDays(2))
                .publiee(true)
                .build());
    }


    private Professeur requireProfesseur(String email) {
        return (Professeur) utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Professeur " + email + " not found during pedagogical seeding"));
    }

    private Administrateur requireAdministrateur(String email) {
        return (Administrateur) utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrateur " + email + " not found during pedagogical seeding"));
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
    private Departement requireDepartement(String codeDepartement) {
        return departementRepository.findByCodeDepartement(codeDepartement)
                .orElseThrow(() -> new RuntimeException("Departement " + codeDepartement + " not found during professor seeding"));
    }
}
