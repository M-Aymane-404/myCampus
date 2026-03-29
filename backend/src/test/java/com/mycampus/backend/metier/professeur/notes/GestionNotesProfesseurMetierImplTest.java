package com.mycampus.backend.metier.professeur.notes;

import com.mycampus.backend.dto.professeur.DonneeEntrer.notes.SaisieNoteEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.notes.FeuilleNotesSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.notes.LigneNoteSortieDTO;
import com.mycampus.backend.entites.AffectationEnseignement;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Examen;
import com.mycampus.backend.entites.Matiere;
import com.mycampus.backend.entites.Resultat;
import com.mycampus.backend.enumerations.EtatExamen;
import com.mycampus.backend.enumerations.EtatInscription;
import com.mycampus.backend.enumerations.NiveauAnnee;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.TypeExamen;
import com.mycampus.backend.exceptions.NotesProfesseurException;
import com.mycampus.backend.mappers.professeur.NotesProfesseurMapper;
import com.mycampus.backend.metier.professeur.StockageFichiersService;
import com.mycampus.backend.repositorys.AffectationEnseignementRepository;
import com.mycampus.backend.repositorys.ClasseRepository;
import com.mycampus.backend.repositorys.EtudiantRepository;
import com.mycampus.backend.repositorys.ExamenRepository;
import com.mycampus.backend.repositorys.ResultatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionNotesProfesseurMetierImplTest {

    private static final String PPR = "PPR-001";
    private static final String CODE_CLASSE = "INFO3-A";

    @Mock
    private ExamenRepository examenRepo;
    @Mock
    private ClasseRepository classeRepo;
    @Mock
    private EtudiantRepository etudiantRepo;
    @Mock
    private ResultatRepository resultatRepo;
    @Mock
    private AffectationEnseignementRepository affectationRepo;
    @Mock
    private NotesProfesseurMapper notesProfesseurMapper;
    @Mock
    private StockageFichiersService stockage;

    @InjectMocks
    private GestionNotesProfesseurMetierImpl metier;

    private Examen examen;
    private Classe classe;
    private Etudiant etudiant1;
    private Etudiant etudiant2;

    @BeforeEach
    void setUp() {
        Matiere matiere = Matiere.builder()
                .id(10L)
                .codeMatiere("SPRING")
                .nom("Spring Boot")
                .build();

        examen = Examen.builder()
                .id(20L)
                .matiere(matiere)
                .semestre(Semestre.S1)
                .numeroExamen(1)
                .typeExamen(TypeExamen.NORMAL)
                .dateExamen(new Date())
                .duree(120)
                .etat(EtatExamen.PROGRAMME)
                .build();

        classe = Classe.builder()
                .id(30L)
                .codeClasse(CODE_CLASSE)
                .nom("Informatique 3A")
                .niveau(NiveauAnnee.A3)
                .anneeUniversitaire("2025/2026")
                .effectifMax(30)
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
    void chargerFeuilleNotes_ProfesseurAutorise_RetourneLignesEtStats() {
        Resultat resultat = Resultat.builder()
                .id(501L)
                .examen(examen)
                .etudiant(etudiant1)
                .note(14.0)
                .build();

        LigneNoteSortieDTO ligne1 = LigneNoteSortieDTO.builder().idResultat(501L).idEtudiant(100L).note(14.0).build();
        LigneNoteSortieDTO ligne2 = LigneNoteSortieDTO.builder().idEtudiant(101L).note(null).build();

        when(examenRepo.findByIdWithMatiere(20L)).thenReturn(Optional.of(examen));
        when(affectationRepo.findActiveByProfClasseMatiere(PPR, CODE_CLASSE, "SPRING"))
                .thenReturn(List.of(AffectationEnseignement.builder().id(1L).build()));
        when(classeRepo.findByCodeClasse(CODE_CLASSE)).thenReturn(Optional.of(classe));
        when(etudiantRepo.findByClasse_CodeClasseOrderByNomAscPrenomAsc(CODE_CLASSE))
                .thenReturn(List.of(etudiant1, etudiant2));
        when(resultatRepo.findAllByExamenId(20L)).thenReturn(List.of(resultat));
        when(notesProfesseurMapper.toLigneDTO(etudiant1, resultat)).thenReturn(ligne1);
        when(notesProfesseurMapper.toLigneDTO(etudiant2, null)).thenReturn(ligne2);

        FeuilleNotesSortieDTO feuille = metier.chargerFeuilleNotes(PPR, CODE_CLASSE, 20L);

        assertNotNull(feuille);
        assertEquals(CODE_CLASSE, feuille.getCodeClasse());
        assertEquals("Spring Boot", feuille.getNomMatiere());
        assertEquals(2, feuille.getLignes().size());
        assertEquals(2, feuille.getStats().getTotalEtudiants());
        assertEquals(1, feuille.getStats().getNbNotesSaisies());
        assertEquals(14.0, feuille.getStats().getMoyenne());
    }

    @Test
    void autosaveNote_ResultatAbsent_CreeNouveauResultat() {
        SaisieNoteEntrerDTO dto = SaisieNoteEntrerDTO.builder().idEtudiant(100L).note(16.5).build();
        LigneNoteSortieDTO ligne = LigneNoteSortieDTO.builder().idEtudiant(100L).note(16.5).build();

        when(examenRepo.findByIdWithMatiere(20L)).thenReturn(Optional.of(examen));
        when(affectationRepo.findActiveByProfClasseMatiere(PPR, CODE_CLASSE, "SPRING"))
                .thenReturn(List.of(AffectationEnseignement.builder().id(1L).build()));
        when(etudiantRepo.findById(100L)).thenReturn(Optional.of(etudiant1));
        when(etudiantRepo.existsByIdAndClasse_CodeClasse(100L, CODE_CLASSE)).thenReturn(true);
        when(resultatRepo.findByExamen_IdAndEtudiant_Id(20L, 100L)).thenReturn(Optional.empty());
        when(resultatRepo.save(any(Resultat.class))).thenAnswer(invocation -> {
            Resultat saved = invocation.getArgument(0);
            saved.setId(801L);
            return saved;
        });
        when(notesProfesseurMapper.toLigneDTO(any(Etudiant.class), any(Resultat.class))).thenReturn(ligne);

        LigneNoteSortieDTO resultat = metier.autosaveNote(PPR, CODE_CLASSE, 20L, dto);

        assertEquals(16.5, resultat.getNote());

        ArgumentCaptor<Resultat> captor = ArgumentCaptor.forClass(Resultat.class);
        verify(resultatRepo).save(captor.capture());
        Resultat resultatSauvegarde = captor.getValue();
        assertEquals(examen, resultatSauvegarde.getExamen());
        assertEquals(etudiant1, resultatSauvegarde.getEtudiant());
        assertEquals(16.5, resultatSauvegarde.getNote());
        assertNull(resultatSauvegarde.getDatePublication());
    }

    @Test
    void autosaveNote_ProfesseurNonAutorise_ThrowsExceptionEtNeSauvegardePas() {
        SaisieNoteEntrerDTO dto = SaisieNoteEntrerDTO.builder().idEtudiant(100L).note(12.0).build();

        when(examenRepo.findByIdWithMatiere(20L)).thenReturn(Optional.of(examen));
        when(affectationRepo.findActiveByProfClasseMatiere(PPR, CODE_CLASSE, "SPRING")).thenReturn(List.of());

        NotesProfesseurException exception = assertThrows(
                NotesProfesseurException.class,
                () -> metier.autosaveNote(PPR, CODE_CLASSE, 20L, dto)
        );

        assertEquals("Accès refusé: affectation inexistante ou inactive.", exception.getMessage());
        verify(resultatRepo, never()).save(any(Resultat.class));
    }

    @Test
    void publierNotes_ResultatsExistants_PublieNotesEtMetExamenACorrige() {
        Resultat resultatPublie = Resultat.builder().id(901L).examen(examen).etudiant(etudiant1).note(15.0).build();
        Resultat resultatSansNote = Resultat.builder().id(902L).examen(examen).etudiant(etudiant2).note(null).build();

        when(examenRepo.findByIdWithMatiere(20L)).thenReturn(Optional.of(examen));
        when(affectationRepo.findActiveByProfClasseMatiere(PPR, CODE_CLASSE, "SPRING"))
                .thenReturn(List.of(AffectationEnseignement.builder().id(1L).build()));
        when(resultatRepo.findAllByExamenId(20L)).thenReturn(List.of(resultatPublie, resultatSansNote));

        int nombrePublie = metier.publierNotes(PPR, CODE_CLASSE, 20L);

        assertEquals(1, nombrePublie);
        assertNotNull(resultatPublie.getDatePublication());
        assertEquals(EtatExamen.CORRIGE, examen.getEtat());
        verify(resultatRepo).saveAll(List.of(resultatPublie, resultatSansNote));
        verify(examenRepo).save(examen);
    }

    @Test
    void deposerCopieCorrigeeEtudiant_EtudiantHorsClasse_ThrowsExceptionEtNeStockePas() {
        MultipartFile pdf = org.mockito.Mockito.mock(MultipartFile.class);

        when(examenRepo.findByIdWithMatiere(20L)).thenReturn(Optional.of(examen));
        when(affectationRepo.findActiveByProfClasseMatiere(PPR, CODE_CLASSE, "SPRING"))
                .thenReturn(List.of(AffectationEnseignement.builder().id(1L).build()));
        when(etudiantRepo.existsByIdAndClasse_CodeClasse(100L, CODE_CLASSE)).thenReturn(false);

        NotesProfesseurException exception = assertThrows(
                NotesProfesseurException.class,
                () -> metier.deposerCopieCorrigeeEtudiant(PPR, CODE_CLASSE, 20L, 100L, pdf)
        );

        assertEquals("Étudiant ne fait pas partie de la classe.", exception.getMessage());
        verify(stockage, never()).sauvegarderPdf(any(MultipartFile.class), any(String.class));
        verify(resultatRepo, never()).save(any(Resultat.class));
    }

    @Test
    void autosaveNote_EtudiantInexistant_ThrowsExceptionEtNeSauvegardePas() {
        SaisieNoteEntrerDTO dto = SaisieNoteEntrerDTO.builder().idEtudiant(999L).note(10.0).build();

        when(examenRepo.findByIdWithMatiere(20L)).thenReturn(Optional.of(examen));
        when(affectationRepo.findActiveByProfClasseMatiere(PPR, CODE_CLASSE, "SPRING"))
                .thenReturn(List.of(AffectationEnseignement.builder().id(1L).build()));
        when(etudiantRepo.findById(999L)).thenReturn(Optional.empty());

        NotesProfesseurException exception = assertThrows(
                NotesProfesseurException.class,
                () -> metier.autosaveNote(PPR, CODE_CLASSE, 20L, dto)
        );

        assertEquals("Étudiant introuvable.", exception.getMessage());
        verify(resultatRepo, never()).save(any(Resultat.class));
    }
}
