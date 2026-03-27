package com.mycampus.backend.integration;

import com.mycampus.backend.dto.professeur.DonneeEntrer.notes.SaisieNoteEntrerDTO;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Examen;
import com.mycampus.backend.entites.Matiere;
import com.mycampus.backend.entites.Resultat;
import com.mycampus.backend.enumerations.EtatExamen;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.TypeExamen;
import com.mycampus.backend.exceptions.NotesProfesseurException;
import com.mycampus.backend.metier.professeur.notes.GestionNotesProfesseurMetier;
import com.mycampus.backend.repositorys.EtudiantRepository;
import com.mycampus.backend.repositorys.ExamenRepository;
import com.mycampus.backend.repositorys.MatiereRepository;
import com.mycampus.backend.repositorys.ResultatRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GestionNotesIntegrationTest {

    private static final String PPR_PROF_1 = "20001200";
    private static final String CODE_CLASSE = "ILCS1-A";
    private static final String APOGEE_ETUDIANT = "120300100";
    private static final String MATIERE_PROF_1 = "ILCS-POO-S1-M1";
    private static final String MATIERE_PROF_2 = "ILCS-BDD-S1-M1";

    @Autowired
    private GestionNotesProfesseurMetier gestionNotesProfesseurMetier;

    @Autowired
    private ExamenRepository examenRepository;

    @Autowired
    private ResultatRepository resultatRepository;

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private MatiereRepository matiereRepository;

    @Test
    void autosaveNote_ParcoursNominal_PersisteResultatEnBase() {
        Etudiant etudiant = etudiantRepository.findByApogee(APOGEE_ETUDIANT).orElseThrow();
        Examen examen = creerExamenUnique(MATIERE_PROF_1, 70);

        SaisieNoteEntrerDTO dto = SaisieNoteEntrerDTO.builder()
                .idEtudiant(etudiant.getId())
                .note(15.5)
                .build();

        gestionNotesProfesseurMetier.autosaveNote(PPR_PROF_1, CODE_CLASSE, examen.getId(), dto);

        Resultat resultat = resultatRepository.findByExamen_IdAndEtudiant_Id(examen.getId(), etudiant.getId())
                .orElseThrow();

        assertEquals(15.5, resultat.getNote());
        assertNull(resultat.getDatePublication());
        assertEquals(examen.getId(), resultat.getExamen().getId());
    }

    @Test
    void autosaveNote_ProfesseurNonAutorise_RejecteAccesEtNePersisteAucunResultat() {
        Etudiant etudiant = etudiantRepository.findByApogee(APOGEE_ETUDIANT).orElseThrow();
        Examen examen = creerExamenUnique(MATIERE_PROF_2, 71);

        SaisieNoteEntrerDTO dto = SaisieNoteEntrerDTO.builder()
                .idEtudiant(etudiant.getId())
                .note(12.0)
                .build();

        assertThrows(
                NotesProfesseurException.class,
                () -> gestionNotesProfesseurMetier.autosaveNote(PPR_PROF_1, CODE_CLASSE, examen.getId(), dto)
        );

        assertEquals(
                false,
                resultatRepository.findByExamen_IdAndEtudiant_Id(examen.getId(), etudiant.getId()).isPresent()
        );
    }

    @Test
    void publierNotes_TransactionMetier_MetAJourResultatEtEtatExamen() {
        Etudiant etudiant = etudiantRepository.findByApogee(APOGEE_ETUDIANT).orElseThrow();
        Examen examen = creerExamenUnique(MATIERE_PROF_1, 72);

        Resultat draft = Resultat.builder()
                .etudiant(etudiant)
                .examen(examen)
                .note(18.0)
                .datePublication(null)
                .build();
        resultatRepository.save(draft);

        int nbPublies = gestionNotesProfesseurMetier.publierNotes(PPR_PROF_1, CODE_CLASSE, examen.getId());

        Resultat persisted = resultatRepository.findByExamen_IdAndEtudiant_Id(examen.getId(), etudiant.getId())
                .orElseThrow();
        Examen examenPersisted = examenRepository.findById(examen.getId()).orElseThrow();

        assertEquals(1, nbPublies);
        assertNotNull(persisted.getDatePublication());
        assertEquals(EtatExamen.CORRIGE, examenPersisted.getEtat());
    }

    private Examen creerExamenUnique(String codeMatiere, int numeroExamen) {
        Matiere matiere = matiereRepository.findByCodeMatiere(codeMatiere).orElseThrow();
        Examen examen = Examen.builder()
                .matiere(matiere)
                .semestre(Semestre.S1)
                .dateExamen(Date.from(LocalDateTime.of(2027, 3, 10, 10, 0)
                        .atZone(ZoneId.systemDefault()).toInstant()))
                .numeroExamen(numeroExamen)
                .typeExamen(TypeExamen.RATTRAPAGE)
                .duree(120)
                .etat(EtatExamen.PROGRAMME)
                .build();
        return examenRepository.save(examen);
    }
}
