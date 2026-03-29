package com.mycampus.backend.metier.professeur.presences;

import com.mycampus.backend.dto.professeur.DonneeEntrer.presences.PresenceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.FeuilleAppelPresenceSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.LignePresenceSortieDTO;
import com.mycampus.backend.entites.AffectationEnseignement;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Matiere;
import com.mycampus.backend.entites.Presence;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.entites.Seance;
import com.mycampus.backend.enumerations.EtatInscription;
import com.mycampus.backend.enumerations.NiveauAnnee;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.StatutJustificatif;
import com.mycampus.backend.enumerations.StatusAbsence;
import com.mycampus.backend.enumerations.TypeMotif;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.metier.etudiant.motifDePresence.GestionDeMotifDePresenceMetier;
import com.mycampus.backend.metier.security.LienFichierSecuriseService;
import com.mycampus.backend.repositorys.AffectationEnseignementRepository;
import com.mycampus.backend.repositorys.ClasseRepository;
import com.mycampus.backend.repositorys.EtudiantRepository;
import com.mycampus.backend.repositorys.PresenceRepository;
import com.mycampus.backend.repositorys.ProfesseurRepository;
import com.mycampus.backend.repositorys.SeanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionPresencesProfesseurMetierImplTest {

    private static final String PPR = "PPR-001";
    private static final Long SEANCE_ID = 10L;

    @Mock
    private PresenceRepository presenceRepository;
    @Mock
    private SeanceRepository seanceRepository;
    @Mock
    private EtudiantRepository etudiantRepository;
    @Mock
    private ProfesseurRepository professeurRepository;
    @Mock
    private ClasseRepository classeRepository;
    @Mock
    private AffectationEnseignementRepository affectationEnseignementRepository;
    @Mock
    private GestionDeMotifDePresenceMetier gestionDeMotifDePresenceMetier;
    @Mock
    private LienFichierSecuriseService lienFichierSecuriseService;

    @InjectMocks
    private GestionPresencesProfesseurMetierImpl metier;

    private Professeur professeur;
    private Classe classe;
    private Matiere matiere;
    private Seance seance;
    private Etudiant etudiant1;
    private Etudiant etudiant2;

    @BeforeEach
    void setUp() {
        professeur = Professeur.builder()
                .id(1L)
                .ppr(PPR)
                .nom("Prof")
                .prenom("One")
                .email("prof@mycampus.ma")
                .password("secret")
                .cin("AA1")
                .specialite("Informatique")
                .grade("PES")
                .active(true)
                .build();

        classe = Classe.builder()
                .id(2L)
                .codeClasse("INFO3-A")
                .nom("Informatique 3A")
                .niveau(NiveauAnnee.A3)
                .anneeUniversitaire("2025/2026")
                .effectifMax(30)
                .build();

        matiere = Matiere.builder()
                .id(3L)
                .codeMatiere("SPRING")
                .nom("Spring Boot")
                .build();

        AffectationEnseignement affectation = AffectationEnseignement.builder()
                .id(4L)
                .professeur(professeur)
                .classe(classe)
                .matiere(matiere)
                .active(true)
                .anneeUniversitaire("2025/2026")
                .build();

        seance = Seance.builder()
                .id(SEANCE_ID)
                .date(LocalDate.of(2026, 3, 20))
                .heureDebut(LocalDateTime.of(2026, 3, 20, 8, 0))
                .heureFin(LocalDateTime.of(2026, 3, 20, 10, 0))
                .salle("B12")
                .affectationEnseignement(affectation)
                .build();

        etudiant1 = Etudiant.builder()
                .id(100L)
                .apogee("A100")
                .nom("Amine")
                .prenom("Ali")
                .email("amine@etu.ma")
                .password("pwd")
                .cin("CIN100")
                .niveau(NiveauAnnee.A3)
                .anneeInscription(2025)
                .semestreActuel(Semestre.S1)
                .etatInscription(EtatInscription.INSCRIT)
                .classe(classe)
                .active(true)
                .build();

        etudiant2 = Etudiant.builder()
                .id(101L)
                .apogee("A101")
                .nom("Sara")
                .prenom("El Idrissi")
                .email("sara@etu.ma")
                .password("pwd")
                .cin("CIN101")
                .niveau(NiveauAnnee.A3)
                .anneeInscription(2025)
                .semestreActuel(Semestre.S1)
                .etatInscription(EtatInscription.INSCRIT)
                .classe(classe)
                .active(true)
                .build();
    }

    @Test
    void chargerFeuilleAppelPresence_ProfesseurAutorise_RetourneFeuilleComplete() {
        Presence presence = Presence.builder()
                .id(501L)
                .seance(seance)
                .etudiant(etudiant1)
                .status(StatusAbsence.ABSENT)
                .descriptionDeJustification("Certificat")
                .typeMotif(TypeMotif.CERTIFICAT_MEDICAL)
                .motifNom("certificat.pdf")
                .dateDeclaration(LocalDateTime.of(2026, 3, 20, 11, 0))
                .build();

        when(professeurRepository.findByPpr(PPR)).thenReturn(Optional.of(professeur));
        when(seanceRepository.findByIdAndAffectationEnseignement_Professeur_Ppr(SEANCE_ID, PPR))
                .thenReturn(Optional.of(seance));
        when(etudiantRepository.findByClasseId(classe.getId())).thenReturn(List.of(etudiant1, etudiant2));
        when(presenceRepository.findBySeance_IdAndSeance_AffectationEnseignement_Professeur_Ppr(SEANCE_ID, PPR))
                .thenReturn(List.of(presence));
        when(lienFichierSecuriseService.construireUrlJustificatif(501L, "certificat.pdf"))
                .thenReturn("/secure/501/certificat.pdf");

        FeuilleAppelPresenceSortieDTO resultat = metier.chargerFeuilleAppelPresence(PPR, SEANCE_ID);

        assertNotNull(resultat);
        assertEquals(SEANCE_ID, resultat.getSeanceId());
        assertEquals("INFO3-A", resultat.getCodeClasse());
        assertEquals("Spring Boot", resultat.getNomMatiere());
        assertEquals(2, resultat.getLignes().size());
        assertEquals(StatusAbsence.ABSENT, resultat.getLignes().get(0).getStatus());
        assertTrue(resultat.getLignes().get(0).isJustificationSoumise());
        assertEquals("/secure/501/certificat.pdf", resultat.getLignes().get(0).getMotifUrl());
        assertNull(resultat.getLignes().get(1).getStatus());
    }

    @Test
    void saisirPresences_PresenceExistanteEtudiantPresent_MetAJourEtReinitialiseJustificatif() {
        PresenceEntrerDTO dto = PresenceEntrerDTO.builder()
                .apogee("A100")
                .status(StatusAbsence.PRESENT)
                .build();

        Presence presenceExistante = Presence.builder()
                .id(600L)
                .seance(seance)
                .etudiant(etudiant1)
                .status(StatusAbsence.ABSENT)
                .descriptionDeJustification("Motif")
                .typeMotif(TypeMotif.AUTRE)
                .motifNom("motif.pdf")
                .motifUrl("/old")
                .statutJustificatif(StatutJustificatif.EN_ATTENTE)
                .dateDeclaration(LocalDateTime.of(2026, 3, 19, 9, 0))
                .build();

        when(professeurRepository.findByPpr(PPR)).thenReturn(Optional.of(professeur));
        when(etudiantRepository.findByApogee("A100")).thenReturn(Optional.of(etudiant1));
        when(seanceRepository.findByIdAndAffectationEnseignement_Professeur_Ppr(SEANCE_ID, PPR))
                .thenReturn(Optional.of(seance));
        when(etudiantRepository.TrouverEtudiantExistenClasse(SEANCE_ID, etudiant1.getId())).thenReturn(true);
        when(presenceRepository.findBySeance_IdAndEtudiant_Apogee(SEANCE_ID, "A100"))
                .thenReturn(Optional.of(presenceExistante));
        when(presenceRepository.save(any(Presence.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LignePresenceSortieDTO resultat = metier.saisirPresences(PPR, SEANCE_ID, dto);

        assertNotNull(resultat);
        assertEquals(StatusAbsence.PRESENT, resultat.getStatus());
        assertEquals(StatutJustificatif.AUCUN, resultat.getStatutJustificatif());
        assertFalse(resultat.isJustificationSoumise());

        ArgumentCaptor<Presence> captor = ArgumentCaptor.forClass(Presence.class);
        verify(presenceRepository).save(captor.capture());
        Presence presenceSauvegardee = captor.getValue();
        assertEquals(StatusAbsence.PRESENT, presenceSauvegardee.getStatus());
        assertNull(presenceSauvegardee.getDescriptionDeJustification());
        assertNull(presenceSauvegardee.getTypeMotif());
        assertNull(presenceSauvegardee.getMotifNom());
        assertNull(presenceSauvegardee.getMotifUrl());
        assertEquals(StatutJustificatif.AUCUN, presenceSauvegardee.getStatutJustificatif());
        assertNotNull(presenceSauvegardee.getDateDeclaration());
    }

    @Test
    void saisirPresences_EtudiantHorsClasse_ThrowsExceptionEtNeSauvegardePas() {
        PresenceEntrerDTO dto = PresenceEntrerDTO.builder()
                .apogee("A100")
                .status(StatusAbsence.ABSENT)
                .build();

        when(professeurRepository.findByPpr(PPR)).thenReturn(Optional.of(professeur));
        when(etudiantRepository.findByApogee("A100")).thenReturn(Optional.of(etudiant1));
        when(seanceRepository.findByIdAndAffectationEnseignement_Professeur_Ppr(SEANCE_ID, PPR))
                .thenReturn(Optional.of(seance));
        when(etudiantRepository.TrouverEtudiantExistenClasse(SEANCE_ID, etudiant1.getId())).thenReturn(false);

        SeancesProfesseurException exception = assertThrows(
                SeancesProfesseurException.class,
                () -> metier.saisirPresences(PPR, SEANCE_ID, dto)
        );

        assertEquals("L'etudiant n'appartient pas a la classe de cette seance.", exception.getMessage());
        verify(presenceRepository, never()).save(any(Presence.class));
    }

    @Test
    void validerJustificatif_SansJustificatif_ThrowsExceptionEtNeSauvegardePas() {
        Presence presence = Presence.builder()
                .id(700L)
                .seance(seance)
                .etudiant(etudiant1)
                .status(StatusAbsence.ABSENT)
                .dateDeclaration(LocalDateTime.of(2026, 3, 20, 11, 0))
                .build();

        when(professeurRepository.findByPpr(PPR)).thenReturn(Optional.of(professeur));
        when(seanceRepository.findByIdAndAffectationEnseignement_Professeur_Ppr(SEANCE_ID, PPR))
                .thenReturn(Optional.of(seance));
        when(presenceRepository.findById(700L)).thenReturn(Optional.of(presence));

        SeancesProfesseurException exception = assertThrows(
                SeancesProfesseurException.class,
                () -> metier.validerJustificatif(PPR, SEANCE_ID, 700L)
        );

        assertEquals("Aucun justificatif soumis pour cette absence.", exception.getMessage());
        verify(presenceRepository, never()).save(any(Presence.class));
    }

    @Test
    void refuserJustificatif_PresenceAutorisee_MetStatutRefuse() {
        Presence presence = Presence.builder()
                .id(701L)
                .seance(seance)
                .etudiant(etudiant1)
                .status(StatusAbsence.ABSENT)
                .descriptionDeJustification("Absence justifiee")
                .typeMotif(TypeMotif.RAISON_FAMILIALE)
                .dateDeclaration(LocalDateTime.of(2026, 3, 20, 11, 0))
                .build();

        when(professeurRepository.findByPpr(PPR)).thenReturn(Optional.of(professeur));
        when(seanceRepository.findByIdAndAffectationEnseignement_Professeur_Ppr(SEANCE_ID, PPR))
                .thenReturn(Optional.of(seance));
        when(presenceRepository.findById(701L)).thenReturn(Optional.of(presence));
        when(presenceRepository.save(any(Presence.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LignePresenceSortieDTO resultat = metier.refuserJustificatif(PPR, SEANCE_ID, 701L);

        assertEquals(StatutJustificatif.REFUSE, resultat.getStatutJustificatif());
        verify(presenceRepository).save(presence);
    }
}
