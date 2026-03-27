package com.mycampus.backend.metier.professeur.seances;

import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.CreationSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.ModificationSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.RechercheSeancesEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.seances.SeanceResumeSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.seances.SeanceSortieDTO;
import com.mycampus.backend.entites.AffectationEnseignement;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.FichierSeance;
import com.mycampus.backend.entites.Matiere;
import com.mycampus.backend.entites.Seance;
import com.mycampus.backend.enumerations.EtatSeance;
import com.mycampus.backend.enumerations.NiveauAnnee;
import com.mycampus.backend.enumerations.TypeSeance;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.mappers.administrateur.StructureMapper;
import com.mycampus.backend.mappers.professeur.FichiersSeanceProfesseurMapper;
import com.mycampus.backend.mappers.professeur.SeancesProfesseurMapper;
import com.mycampus.backend.metier.professeur.StockageFichiersService;
import com.mycampus.backend.metier.security.ValidationFichierSecuriseService;
import com.mycampus.backend.repositorys.AffectationEnseignementRepository;
import com.mycampus.backend.repositorys.FichierSeanceRepository;
import com.mycampus.backend.repositorys.SeanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionSeancesProfesseurMetierImplTest {

    private static final String PPR = "PPR-001";

    @Mock
    private SeanceRepository seanceRepository;
    @Mock
    private AffectationEnseignementRepository affectationEnseignementRepository;
    @Mock
    private FichierSeanceRepository fichierSeanceRepository;
    @Mock
    private SeancesProfesseurMapper seancesProfesseurMapper;
    @Mock
    private FichiersSeanceProfesseurMapper fichiersSeanceProfesseurMapper;
    @Mock
    private StructureMapper structureMapper;
    @Mock
    private ValidationFichierSecuriseService validationFichierSecuriseService;
    @Mock
    private StockageFichiersService stockageFichiersService;

    @InjectMocks
    private GestionSeancesProfesseurMetierImpl metier;

    private AffectationEnseignement affectation;
    private Seance seance;

    @BeforeEach
    void setUp() {
        Classe classe = Classe.builder()
                .id(2L)
                .codeClasse("INFO3-A")
                .nom("Informatique 3A")
                .niveau(NiveauAnnee.A3)
                .anneeUniversitaire("2025/2026")
                .effectifMax(30)
                .build();

        Matiere matiere = Matiere.builder()
                .id(3L)
                .codeMatiere("SPRING")
                .nom("Spring Boot")
                .build();

        affectation = AffectationEnseignement.builder()
                .id(4L)
                .classe(classe)
                .matiere(matiere)
                .active(true)
                .anneeUniversitaire("2025/2026")
                .build();

        seance = Seance.builder()
                .id(50L)
                .affectationEnseignement(affectation)
                .type(TypeSeance.COURS)
                .date(LocalDate.of(2026, 3, 20))
                .heureDebut(LocalDateTime.of(2026, 3, 20, 8, 0))
                .heureFin(LocalDateTime.of(2026, 3, 20, 10, 0))
                .salle("B12")
                .etat(EtatSeance.PLANIFIEE)
                .contenu("Introduction Spring")
                .build();
    }

    @Test
    void creerSeance_AffectationActive_CreeSeance() {
        CreationSeanceEntrerDTO dto = CreationSeanceEntrerDTO.builder()
                .codeClasse("INFO3-A")
                .codeMatiere("SPRING")
                .anneeUniversitaire("2025/2026")
                .type(TypeSeance.COURS)
                .date(LocalDate.of(2026, 3, 20))
                .heureDebut(LocalDateTime.of(2026, 3, 20, 8, 0))
                .heureFin(LocalDateTime.of(2026, 3, 20, 10, 0))
                .salle("B12")
                .etat(EtatSeance.PLANIFIEE)
                .contenu("Introduction Spring")
                .build();
        SeanceSortieDTO sortie = SeanceSortieDTO.builder().id(50L).build();

        when(affectationEnseignementRepository
                .findByActiveTrueAndProfesseur_PprAndClasse_CodeClasseAndMatiere_CodeMatiereAndAnneeUniversitaire(
                        PPR, "INFO3-A", "SPRING", "2025/2026"))
                .thenReturn(Optional.of(affectation));
        when(seanceRepository.save(any(Seance.class))).thenReturn(seance);
        when(seancesProfesseurMapper.FromSeanceVersSeanceSortieDTO(seance)).thenReturn(sortie);

        SeanceSortieDTO resultat = metier.creerSeance(PPR, dto);

        assertNotNull(resultat);
        assertEquals(50L, resultat.getId());
        verify(seanceRepository).save(any(Seance.class));
    }

    @Test
    void creerSeance_HorairesIncoherents_ThrowsExceptionEtNeSauvegardePas() {
        CreationSeanceEntrerDTO dto = CreationSeanceEntrerDTO.builder()
                .codeClasse("INFO3-A")
                .codeMatiere("SPRING")
                .anneeUniversitaire("2025/2026")
                .heureDebut(LocalDateTime.of(2026, 3, 20, 10, 0))
                .heureFin(LocalDateTime.of(2026, 3, 20, 8, 0))
                .build();

        SeancesProfesseurException exception = assertThrows(
                SeancesProfesseurException.class,
                () -> metier.creerSeance(PPR, dto)
        );

        assertEquals("L'heure de début doit être strictement avant l'heure de fin.", exception.getMessage());
        verify(seanceRepository, never()).save(any(Seance.class));
    }

    @Test
    void modifierSeance_ProfesseurNonAutorise_ThrowsExceptionEtNeSauvegardePas() {
        ModificationSeanceEntrerDTO dto = ModificationSeanceEntrerDTO.builder()
                .salle("C21")
                .build();

        when(seanceRepository.findByIdAndPprAvecDetails(50L, PPR)).thenReturn(Optional.empty());

        SeancesProfesseurException exception = assertThrows(
                SeancesProfesseurException.class,
                () -> metier.modifierSeance(PPR, 50L, dto)
        );

        assertEquals("Séance introuvable ou non autorisée.", exception.getMessage());
        verify(seanceRepository, never()).save(any(Seance.class));
    }

    @Test
    void supprimerSeance_SeanceAutorisee_SupprimeFichiersEtSeance() {
        FichierSeance fichier = FichierSeance.builder()
                .id(70L)
                .chemin("seances/chap1.pdf")
                .seance(seance)
                .nomOriginal("chap1.pdf")
                .typeMime("application/pdf")
                .taille(100L)
                .dateUpload(LocalDateTime.now())
                .build();

        when(seanceRepository.findByIdAndPprAvecDetails(50L, PPR)).thenReturn(Optional.of(seance));
        when(fichierSeanceRepository.findBySeance_Id(50L)).thenReturn(List.of(fichier));

        metier.supprimerSeance(PPR, 50L);

        verify(stockageFichiersService).supprimerSilencieusement("seances/chap1.pdf");
        verify(fichierSeanceRepository).deleteBySeanceId(50L);
        verify(seanceRepository).delete(seance);
    }

    @Test
    void rechercherSeances_FiltresClasseEtMatiere_RetourneSeancesCorrespondantes() {
        RechercheSeancesEntrerDTO dto = RechercheSeancesEntrerDTO.builder()
                .codeClasse("INFO3-A")
                .codeMatiere("SPRING")
                .build();
        SeanceResumeSortieDTO resume = SeanceResumeSortieDTO.builder().id(50L).salle("B12").build();

        when(seanceRepository.findByPprAvecDetails(PPR)).thenReturn(List.of(seance));
        when(seancesProfesseurMapper.FromSeanceVersSeanceResumeSortieDTO(seance)).thenReturn(resume);

        List<SeanceResumeSortieDTO> resultats = metier.rechercherSeances(PPR, dto);

        assertEquals(1, resultats.size());
        assertEquals(50L, resultats.get(0).getId());
        verify(seanceRepository).findByPprAvecDetails(PPR);
    }
}
