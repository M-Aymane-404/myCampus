package com.mycampus.backend.integration;

import com.mycampus.backend.dto.professeur.DonneeEntrer.examens.ExamenCreationEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.examens.ExamenModificationEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.examens.ExamenSortieDTO;
import com.mycampus.backend.entites.Examen;
import com.mycampus.backend.enumerations.EtatExamen;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.TypeExamen;
import com.mycampus.backend.exceptions.NotesProfesseurException;
import com.mycampus.backend.metier.professeur.examens.GestionExamensProfesseurMetier;
import com.mycampus.backend.repositorys.ExamenRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GestionExamensIntegrationTest {

    private static final String PPR_PROF_1 = "20001200";
    private static final String CODE_CLASSE = "ILCS1-A";
    private static final String MATIERE_PROF_1 = "ILCS-POO-S1-M1";
    private static final String MATIERE_PROF_2 = "ILCS-BDD-S1-M1";

    @Autowired
    private GestionExamensProfesseurMetier gestionExamensProfesseurMetier;

    @Autowired
    private ExamenRepository examenRepository;

    @Test
    void creerExamen_ProfesseurAutorise_PersisteExamenEtRelations() {
        long countAvant = examenRepository.count();

        ExamenCreationEntrerDTO dto = ExamenCreationEntrerDTO.builder()
                .semestre(Semestre.S1)
                .dateExamen(Date.from(LocalDateTime.of(2027, 1, 15, 9, 0)
                        .atZone(ZoneId.systemDefault()).toInstant()))
                .numeroExamen(77)
                .typeExamen(TypeExamen.RATTRAPAGE)
                .duree(120)
                .build();

        ExamenSortieDTO created = gestionExamensProfesseurMetier
                .creerExamen(PPR_PROF_1, CODE_CLASSE, MATIERE_PROF_1, dto);

        assertNotNull(created.getIdExamen());
        assertEquals(countAvant + 1, examenRepository.count());

        Examen persisted = examenRepository.findById(created.getIdExamen()).orElseThrow();
        assertEquals(MATIERE_PROF_1, persisted.getMatiere().getCodeMatiere());
        assertEquals(EtatExamen.PROGRAMME, persisted.getEtat());
        assertEquals(TypeExamen.RATTRAPAGE, persisted.getTypeExamen());
    }

    @Test
    void creerExamen_DoublonMetier_RejecteCreationEtNePersisteRien() {
        long countAvant = examenRepository.count();

        ExamenCreationEntrerDTO dto = ExamenCreationEntrerDTO.builder()
                .semestre(Semestre.S1)
                .dateExamen(Date.from(LocalDateTime.of(2027, 2, 1, 10, 0)
                        .atZone(ZoneId.systemDefault()).toInstant()))
                .numeroExamen(1)
                .typeExamen(TypeExamen.NORMAL)
                .duree(90)
                .build();

        assertThrows(
                NotesProfesseurException.class,
                () -> gestionExamensProfesseurMetier.creerExamen(PPR_PROF_1, CODE_CLASSE, MATIERE_PROF_1, dto)
        );

        assertEquals(countAvant, examenRepository.count());
    }

    @Test
    void modifierExamen_ProfesseurNonAutorise_RejecteAccesEtConserveEtatInitial() {
        Examen examen = examenRepository.findByCodeMatiere(MATIERE_PROF_2)
                .stream()
                .findFirst()
                .orElseThrow();
        EtatExamen etatInitial = examen.getEtat();

        ExamenModificationEntrerDTO dto = ExamenModificationEntrerDTO.builder()
                .etat(EtatExamen.CORRIGE)
                .build();

        assertThrows(
                NotesProfesseurException.class,
                () -> gestionExamensProfesseurMetier.modifierExamen(PPR_PROF_1, examen.getId(), dto)
        );

        Examen unchanged = examenRepository.findById(examen.getId()).orElseThrow();
        assertEquals(etatInitial, unchanged.getEtat());
    }
}
