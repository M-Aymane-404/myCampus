package com.mycampus.backend.metier.professeur.examens;

import com.mycampus.backend.dto.professeur.DonneeEntrer.examens.ExamenCreationEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.examens.ExamenModificationEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.examens.ExamenSortieDTO;
import com.mycampus.backend.entites.AffectationEnseignement;
import com.mycampus.backend.entites.Examen;
import com.mycampus.backend.entites.Matiere;
import com.mycampus.backend.entites.Module;
import com.mycampus.backend.enumerations.EtatExamen;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.TypeExamen;
import com.mycampus.backend.exceptions.NotesProfesseurException;
import com.mycampus.backend.metier.professeur.StockageFichiersService;
import com.mycampus.backend.repositorys.AffectationEnseignementRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionExamensProfesseurMetierImplTest {

    private static final String PPR = "PPR-001";
    private static final String CODE_CLASSE = "INFO3-A";
    private static final String CODE_MATIERE = "SPRING";

    @Mock
    private AffectationEnseignementRepository affectationRepo;
    @Mock
    private ExamenRepository examenRepo;
    @Mock
    private ResultatRepository resultatRepo;
    @Mock
    private StockageFichiersService stockage;

    @InjectMocks
    private GestionExamensProfesseurMetierImpl metier;

    private AffectationEnseignement affectation;
    private Matiere matiere;
    private Date dateExamen;

    @BeforeEach
    void setUp() {
        Module module = Module.builder()
                .id(10L)
                .codeModule("MOD-SPRING")
                .nom("Architecture Java")
                .semestre(Semestre.S1)
                .build();

        matiere = Matiere.builder()
                .id(11L)
                .codeMatiere(CODE_MATIERE)
                .nom("Spring Boot")
                .module(module)
                .build();

        affectation = AffectationEnseignement.builder()
                .id(12L)
                .matiere(matiere)
                .active(true)
                .anneeUniversitaire("2025/2026")
                .build();

        dateExamen = new Date();
    }

    @Test
    void creerExamen_AffectationActiveEtDonneesValides_CreeExamenProgramme() {
        ExamenCreationEntrerDTO dto = ExamenCreationEntrerDTO.builder()
                .dateExamen(dateExamen)
                .numeroExamen(1)
                .typeExamen(TypeExamen.NORMAL)
                .duree(120)
                .build();

        when(affectationRepo.findActiveByProfClasseMatiere(PPR, CODE_CLASSE, CODE_MATIERE))
                .thenReturn(List.of(affectation));
        when(examenRepo.existsByMatiere_CodeMatiereAndSemestreAndNumeroExamenAndTypeExamen(
                CODE_MATIERE, Semestre.S1, 1, TypeExamen.NORMAL))
                .thenReturn(false);
        when(examenRepo.save(any(Examen.class))).thenAnswer(invocation -> {
            Examen examen = invocation.getArgument(0);
            examen.setId(100L);
            return examen;
        });

        ExamenSortieDTO resultat = metier.creerExamen(PPR, CODE_CLASSE, CODE_MATIERE, dto);

        assertEquals(100L, resultat.getIdExamen());
        assertEquals(CODE_MATIERE, resultat.getCodeMatiere());
        assertEquals(EtatExamen.PROGRAMME, resultat.getEtat());
        assertEquals(Semestre.S1, resultat.getSemestre());

        ArgumentCaptor<Examen> captor = ArgumentCaptor.forClass(Examen.class);
        verify(examenRepo).save(captor.capture());
        Examen examenSauvegarde = captor.getValue();
        assertEquals(EtatExamen.PROGRAMME, examenSauvegarde.getEtat());
        assertEquals(matiere, examenSauvegarde.getMatiere());
    }

    @Test
    void creerExamen_ExamenDuplique_ThrowsExceptionEtNeSauvegardePas() {
        ExamenCreationEntrerDTO dto = ExamenCreationEntrerDTO.builder()
                .semestre(Semestre.S1)
                .dateExamen(dateExamen)
                .numeroExamen(1)
                .typeExamen(TypeExamen.NORMAL)
                .duree(120)
                .build();

        when(affectationRepo.findActiveByProfClasseMatiere(PPR, CODE_CLASSE, CODE_MATIERE))
                .thenReturn(List.of(affectation));
        when(examenRepo.existsByMatiere_CodeMatiereAndSemestreAndNumeroExamenAndTypeExamen(
                CODE_MATIERE, Semestre.S1, 1, TypeExamen.NORMAL))
                .thenReturn(true);

        NotesProfesseurException exception = assertThrows(
                NotesProfesseurException.class,
                () -> metier.creerExamen(PPR, CODE_CLASSE, CODE_MATIERE, dto)
        );

        assertEquals("Examen déjà existant (même matière/semestre/numéro/type).", exception.getMessage());
        verify(examenRepo, never()).save(any(Examen.class));
    }

    @Test
    void modifierExamen_ProfesseurNonAffecte_ThrowsExceptionEtNeSauvegardePas() {
        Examen examen = Examen.builder()
                .id(200L)
                .matiere(matiere)
                .semestre(Semestre.S1)
                .numeroExamen(1)
                .typeExamen(TypeExamen.NORMAL)
                .dateExamen(dateExamen)
                .duree(90)
                .etat(EtatExamen.PROGRAMME)
                .build();

        ExamenModificationEntrerDTO dto = ExamenModificationEntrerDTO.builder()
                .etat(EtatExamen.CORRIGE)
                .build();

        when(examenRepo.findByIdWithMatiere(200L)).thenReturn(Optional.of(examen));
        when(affectationRepo.findActivesParProfesseurAvecDetails(PPR)).thenReturn(List.of());

        NotesProfesseurException exception = assertThrows(
                NotesProfesseurException.class,
                () -> metier.modifierExamen(PPR, 200L, dto)
        );

        assertEquals("Accès refusé: vous n'êtes pas affecté à cette matière.", exception.getMessage());
        verify(examenRepo, never()).save(any(Examen.class));
    }

    @Test
    void supprimerExamen_ResultatsExistants_ThrowsExceptionEtNeSupprimePas() {
        Examen examen = Examen.builder()
                .id(201L)
                .matiere(matiere)
                .build();

        when(examenRepo.findByIdWithMatiere(201L)).thenReturn(Optional.of(examen));
        when(affectationRepo.findActivesParProfesseurAvecDetails(PPR)).thenReturn(List.of(affectation));
        when(resultatRepo.existsByExamen_Id(201L)).thenReturn(true);

        NotesProfesseurException exception = assertThrows(
                NotesProfesseurException.class,
                () -> metier.supprimerExamen(PPR, 201L)
        );

        assertEquals("Suppression impossible: des résultats existent déjà.", exception.getMessage());
        verify(examenRepo, never()).delete(any(Examen.class));
    }

    @Test
    void deposerCorrigeExamen_RemplacementCorrige_SupprimeAncienFichier() {
        MultipartFile pdf = org.mockito.Mockito.mock(MultipartFile.class);
        Examen examen = Examen.builder()
                .id(202L)
                .matiere(matiere)
                .semestre(Semestre.S1)
                .numeroExamen(2)
                .typeExamen(TypeExamen.NORMAL)
                .dateExamen(dateExamen)
                .duree(120)
                .etat(EtatExamen.PROGRAMME)
                .corrigePath("examens/corriges/old.pdf")
                .build();

        when(examenRepo.findByIdWithMatiere(202L)).thenReturn(Optional.of(examen));
        when(affectationRepo.findActivesParProfesseurAvecDetails(PPR)).thenReturn(List.of(affectation));
        when(stockage.sauvegarderPdf(pdf, "examens/corriges")).thenReturn("examens/corriges/new.pdf");
        when(examenRepo.save(examen)).thenReturn(examen);

        ExamenSortieDTO resultat = metier.deposerCorrigeExamen(PPR, 202L, pdf);

        assertTrue(resultat.isCorrigeDisponible());
        assertEquals("examens/corriges/new.pdf", examen.getCorrigePath());
        verify(stockage).supprimerSilencieusement("examens/corriges/old.pdf");
        verify(examenRepo).save(examen);
    }
}
