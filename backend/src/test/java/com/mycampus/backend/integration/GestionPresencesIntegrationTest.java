package com.mycampus.backend.integration;

import com.mycampus.backend.dto.professeur.DonneeEntrer.presences.PresenceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.FeuilleAppelPresenceSortieDTO;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Presence;
import com.mycampus.backend.entites.Seance;
import com.mycampus.backend.enumerations.StatusAbsence;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.metier.professeur.presences.GestionPresencesProfesseurMetier;
import com.mycampus.backend.repositorys.EtudiantRepository;
import com.mycampus.backend.repositorys.PresenceRepository;
import com.mycampus.backend.repositorys.SeanceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GestionPresencesIntegrationTest {

    private static final String PPR_PROF_1 = "20001200";
    private static final String PPR_PROF_2 = "30001200";
    private static final String APOGEE_ETUDIANT = "120300100";

    @Autowired
    private GestionPresencesProfesseurMetier gestionPresencesProfesseurMetier;

    @Autowired
    private SeanceRepository seanceRepository;

    @Autowired
    private PresenceRepository presenceRepository;

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Test
    void chargerFeuilleAppelPresence_SeanceAutorisee_ChargeDonneesDepuisBase() {
        Seance seance = trouverUneSeanceDuProfesseur(PPR_PROF_1);

        FeuilleAppelPresenceSortieDTO feuille = gestionPresencesProfesseurMetier
                .chargerFeuilleAppelPresence(PPR_PROF_1, seance.getId());

        List<Etudiant> etudiantsClasse = etudiantRepository.findByClasseId(
                seance.getAffectationEnseignement().getClasse().getId()
        );

        assertNotNull(feuille);
        assertEquals(seance.getId(), feuille.getSeanceId());
        assertEquals(seance.getAffectationEnseignement().getClasse().getCodeClasse(), feuille.getCodeClasse());
        assertEquals(seance.getAffectationEnseignement().getMatiere().getNom(), feuille.getNomMatiere());
        assertEquals(etudiantsClasse.size(), feuille.getLignes().size());
    }

    @Test
    void saisirPresences_ProfesseurAutorise_PersisteModificationEnBase() {
        Seance seance = trouverUneSeanceDuProfesseur(PPR_PROF_1);
        Etudiant etudiant = etudiantRepository.findByApogee(APOGEE_ETUDIANT).orElseThrow();

        PresenceEntrerDTO dto = PresenceEntrerDTO.builder()
                .apogee(etudiant.getApogee())
                .status(StatusAbsence.ABSENT)
                .build();

        gestionPresencesProfesseurMetier.saisirPresences(PPR_PROF_1, seance.getId(), dto);

        Presence presencePersisted = presenceRepository
                .findBySeance_IdAndEtudiant_Apogee(seance.getId(), etudiant.getApogee())
                .orElseThrow();

        assertEquals(StatusAbsence.ABSENT, presencePersisted.getStatus());
    }

    @Test
    void saisirPresences_ProfesseurNonAutorise_RejecteAccesEtNeModifiePasLaBase() {
        Seance seance = trouverUneSeanceDuProfesseur(PPR_PROF_2);
        Etudiant etudiant = etudiantRepository.findByApogee(APOGEE_ETUDIANT).orElseThrow();

        Presence presenceAvant = presenceRepository
                .findBySeance_IdAndEtudiant_Apogee(seance.getId(), etudiant.getApogee())
                .orElseThrow();
        StatusAbsence statusInitial = presenceAvant.getStatus();

        PresenceEntrerDTO dto = PresenceEntrerDTO.builder()
                .apogee(etudiant.getApogee())
                .status(statusInitial == StatusAbsence.PRESENT ? StatusAbsence.ABSENT : StatusAbsence.PRESENT)
                .build();

        assertThrows(
                SeancesProfesseurException.class,
                () -> gestionPresencesProfesseurMetier.saisirPresences(PPR_PROF_1, seance.getId(), dto)
        );

        Presence presenceApres = presenceRepository
                .findBySeance_IdAndEtudiant_Apogee(seance.getId(), etudiant.getApogee())
                .orElseThrow();

        assertEquals(statusInitial, presenceApres.getStatus());
    }

    private Seance trouverUneSeanceDuProfesseur(String ppr) {
        return seanceRepository.findByAffectationEnseignement_Professeur_Ppr(ppr)
                .stream()
                .findFirst()
                .orElseThrow();
    }
}
