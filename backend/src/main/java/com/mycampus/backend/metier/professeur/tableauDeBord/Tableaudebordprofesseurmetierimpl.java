package com.mycampus.backend.metier.professeur.tableauDeBord;

import com.mycampus.backend.dto.professeur.DonneeSortie.tableauDeBord.TableauDeBordProfesseurSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.tableauDeBord.TableauDeBordProfesseurSortieDTO.EnteteProfesseurDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.tableauDeBord.TableauDeBordProfesseurSortieDTO.ExamenResumeDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.tableauDeBord.TableauDeBordProfesseurSortieDTO.SeanceResumeDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.tableauDeBord.TableauDeBordProfesseurSortieDTO.StatsRapidesProfesseurDTO;
import com.mycampus.backend.entites.AffectationEnseignement;
import com.mycampus.backend.entites.Examen;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.entites.Seance;
import com.mycampus.backend.enumerations.EtatExamen;
import com.mycampus.backend.enumerations.EtatSeance;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.repositorys.AffectationEnseignementRepository;
import com.mycampus.backend.repositorys.AnnonceRepository;
import com.mycampus.backend.repositorys.ExamenRepository;
import com.mycampus.backend.repositorys.ProfesseurRepository;
import com.mycampus.backend.repositorys.SeanceRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TableauDeBordProfesseurMetierImpl implements TableauDeBordProfesseurMetier {

    private final ProfesseurRepository professeurRepository;
    private final AffectationEnseignementRepository affectationRepo;
    private final SeanceRepository seanceRepository;
    private final ExamenRepository examenRepository;
    private final AnnonceRepository annonceRepository;

    @Override
    public TableauDeBordProfesseurSortieDTO obtenirTableauDeBord(Long professeurId) {
        Professeur prof = professeurRepository.findById(professeurId)
                .orElseThrow(() -> new UtilisateursException("Professeur introuvable."));

        String ppr = prof.getPpr();

        EnteteProfesseurDTO entete = EnteteProfesseurDTO.builder()
                .nomComplet(prof.getNom() + " " + prof.getPrenom())
                .ppr(ppr)
                .grade(prof.getGrade())
                .specialite(prof.getSpecialite())
                .departement(prof.getDepartementOuTravail() != null
                        ? prof.getDepartementOuTravail().getCodeDepartement()
                        : "Non assigne")
                .statut(prof.getStatut() != null ? prof.getStatut() : "Actif")
                .build();

        List<AffectationEnseignement> affectations = affectationRepo.findActivesParProfesseurAvecDetails(ppr);
        List<Seance> toutesSeances = seanceRepository.findByPprAvecDetails(ppr);

        long nbSeancesTerminees = toutesSeances.stream()
                .filter(seance -> seance.getEtat() == EtatSeance.TERMINEE)
                .count();

        List<SeanceResumeDTO> seancesRecentes = toutesSeances.stream()
                .sorted(Comparator.comparing(
                        seance -> seance.getDate() != null ? seance.getDate() : LocalDate.MIN,
                        Comparator.reverseOrder()))
                .limit(5)
                .map(seance -> {
                    AffectationEnseignement ae = seance.getAffectationEnseignement();
                    return SeanceResumeDTO.builder()
                            .id(seance.getId())
                            .type(seance.getType())
                            .date(seance.getDate())
                            .heureDebut(seance.getHeureDebut())
                            .heureFin(seance.getHeureFin())
                            .salle(seance.getSalle())
                            .etat(seance.getEtat())
                            .nomMatiere(ae != null && ae.getMatiere() != null ? ae.getMatiere().getNom() : "-")
                            .nomClasse(ae != null && ae.getClasse() != null ? ae.getClasse().getNom() : "-")
                            .codeClasse(ae != null && ae.getClasse() != null ? ae.getClasse().getCodeClasse() : "-")
                            .build();
                })
                .collect(Collectors.toList());

        List<String> codesMatieres = affectations.stream()
                .filter(ae -> ae.getMatiere() != null)
                .map(ae -> ae.getMatiere().getCodeMatiere())
                .distinct()
                .collect(Collectors.toList());

        List<Examen> tousExamens = new ArrayList<>();
        for (String codeMatiere : codesMatieres) {
            tousExamens.addAll(examenRepository.findByCodeMatiere(codeMatiere));
        }

        long nbExamensProgrammes = tousExamens.stream()
                .filter(examen -> examen.getEtat() == EtatExamen.PROGRAMME)
                .count();

        List<ExamenResumeDTO> examensAVenir = tousExamens.stream()
                .filter(examen -> examen.getEtat() == EtatExamen.PROGRAMME)
                .sorted(Comparator.comparing(Examen::getDateExamen, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(4)
                .map(examen -> {
                    String nomClasse = affectations.stream()
                            .filter(ae -> ae.getMatiere() != null
                                    && ae.getMatiere().getCodeMatiere().equals(examen.getMatiere().getCodeMatiere()))
                            .findFirst()
                            .map(ae -> ae.getClasse() != null ? ae.getClasse().getNom() : "-")
                            .orElse("-");

                    return ExamenResumeDTO.builder()
                            .id(examen.getId())
                            .nomMatiere(examen.getMatiere() != null ? examen.getMatiere().getNom() : "-")
                            .codeMatiere(examen.getMatiere() != null ? examen.getMatiere().getCodeMatiere() : "-")
                            .nomClasse(nomClasse)
                            .typeExamen(examen.getTypeExamen())
                            .numeroExamen(examen.getNumeroExamen() != null ? examen.getNumeroExamen() : 0)
                            .dateExamen(examen.getDateExamen())
                            .duree(examen.getDuree() != null ? examen.getDuree() : 0)
                            .etat(examen.getEtat())
                            .build();
                })
                .collect(Collectors.toList());

        long nbAnnoncesPubliees = annonceRepository.findByAuteur_Id(prof.getId()).stream()
                .filter(annonce -> Boolean.TRUE.equals(annonce.isPubliee()))
                .count();

        StatsRapidesProfesseurDTO stats = StatsRapidesProfesseurDTO.builder()
                .nbEnseignements(affectations.size())
                .nbSeancesTotales(toutesSeances.size())
                .nbSeancesTerminees((int) nbSeancesTerminees)
                .nbExamensProgrammes((int) nbExamensProgrammes)
                .nbExamensTotal(tousExamens.size())
                .nbAnnoncesPubiiees((int) nbAnnoncesPubliees)
                .build();

        return TableauDeBordProfesseurSortieDTO.builder()
                .entete(entete)
                .stats(stats)
                .seancesRecentes(seancesRecentes)
                .examensAVenir(examensAVenir)
                .build();
    }
}
