package com.mycampus.backend.metier.etudiant.tableauDeBord;

import com.mycampus.backend.dto.etudiant.tableauDeBord.*;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Presence;
import com.mycampus.backend.entites.Resultat;
import com.mycampus.backend.enumerations.NiveauAnnee;
import com.mycampus.backend.enumerations.StatusAbsence;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.repositorys.EtudiantRepository;
import com.mycampus.backend.repositorys.PresenceRepository;
import com.mycampus.backend.repositorys.ResultatRepository;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TableauDeBordEtudiantMetierImpl implements TableauDeBordEtudiantMetier {

     private final ResultatRepository resultatRepository;
    private final PresenceRepository presenceRepository;
    private final EtudiantRepository etudiantRepository;

    @Override
    @Transactional(readOnly = true)
    public TableauDeBordEtudiantSortieDTO obtenirTableauDeBord(Long idEtudiant) throws  UtilisateursException {
        if (idEtudiant == null ) {
            throw new UtilisateursException("Email étudiant manquant (non authentifié)");
        }

        log.info("Génération du tableau de bord pour l'étudiant: {}", idEtudiant);
        
        Etudiant etudiant =  etudiantRepository.findByIdWithClasseAndFiliere(idEtudiant)
                .orElseThrow(() -> new UtilisateursException("Étudiant non trouvé"));
        
        EnteteEtudiantDTO entete = creerEntete(etudiant);
        List<DerniereNoteDTO> dernieresNotes = obtenirDernieresNotes(etudiant);
        AssiduiteDTO assiduite = calculerAssiduite(etudiant);
        
        return TableauDeBordEtudiantSortieDTO.builder()
                .entete(entete)
                .dernieresNotes(dernieresNotes)
                .assiduite(assiduite)
                .build();
    }

    private EnteteEtudiantDTO creerEntete(Etudiant etudiant) {


        String nomComplet = etudiant.getNom() + " " + etudiant.getPrenom();
        NiveauAnnee niveau = etudiant.getNiveau();
        String filiere = etudiant.getClasse() != null ? etudiant.getClasse().getFiliere().getNom() : "Non assignée";
        String classe = etudiant.getClasse() != null ? etudiant.getClasse().getNom() : "Non assignée";
        String session = "Session d'Automne " + java.time.Year.now().getValue();
        String statut = etudiant.getActive() ? "Active" : "Inactive";
        
        return new EnteteEtudiantDTO(
                nomComplet, niveau, filiere, classe, session, statut);
    }

    private List<DerniereNoteDTO> obtenirDernieresNotes(Etudiant etudiant) {

        List<Resultat> resultats = resultatRepository.findTop3ByEtudiantIdOrderByDatePublicationDesc(etudiant.getId());

        return resultats.stream()
                .limit(5)
                .map(resultat -> {
                    String matiere = resultat.getExamen().getMatiere().getNom();
                    Double note = resultat.getNote();
                    String datePublication = resultat.getDatePublication().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));


                    return new DerniereNoteDTO(
                            matiere, note, datePublication);
                })
                .toList();
    }

    private AssiduiteDTO calculerAssiduite(Etudiant etudiant) {
        List<Presence> presences = presenceRepository.findByEtudiantId(etudiant.getId());
        
        if (presences.isEmpty()) {
            return new AssiduiteDTO(0.0, "INCONNU");
        }
        
        long totalSeances = presences.size();
        long presencesPresentes = presences.stream()
                .filter(p -> p.getStatus() == StatusAbsence.PRESENT)
                .count();
        
        double tauxPresence = totalSeances > 0 ? (double) presencesPresentes / totalSeances * 100 : 0.0;
        
        String labelQualite;
        if (tauxPresence >= 95) {
            labelQualite = "EXCELLENT";
        } else if (tauxPresence >= 85) {
            labelQualite = "MOYEN";
        } else {
            labelQualite = "FAIBLE";
        }
        
        return new AssiduiteDTO(tauxPresence, labelQualite);
    }
}
