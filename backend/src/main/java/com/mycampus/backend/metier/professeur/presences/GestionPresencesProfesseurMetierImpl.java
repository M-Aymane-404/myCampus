package com.mycampus.backend.metier.professeur.presences;



import com.mycampus.backend.dto.professeur.DonneeEntrer.presences.PresenceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.presences.TableauPresencesEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.PresenceSortieDTO;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Presence;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.entites.Seance;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.repositorys.EtudiantRepository;
import com.mycampus.backend.repositorys.PresenceRepository;
import com.mycampus.backend.repositorys.ProfesseurRepository;
import com.mycampus.backend.repositorys.SeanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GestionPresencesProfesseurMetierImpl implements GestionPresencesProfesseurMetier {

    private final PresenceRepository presenceRepository;
    private final SeanceRepository seanceRepository;
    private final EtudiantRepository etudiantRepository;
    private final ProfesseurRepository professeurRepository;

    private void requireProfesseurPpr(String professeurPpr) throws SeancesProfesseurException {
        if (!StringUtils.hasText(professeurPpr)) {
            throw new SeancesProfesseurException("Le PPR du professeur est obligatoire.");
        }
    }

    private Seance requireSeancePourProfesseur(String professeurPpr, Long seanceId) throws SeancesProfesseurException {
        requireProfesseurPpr(professeurPpr);

        if (seanceId == null) {
            throw new SeancesProfesseurException("SeanceId obligatoire.");
        }

        return seanceRepository
                .findByIdAndAffectationEnseignement_Professeur_Ppr(seanceId, professeurPpr)
                .orElseThrow(() -> new SeancesProfesseurException("Séance introuvable ou non autorisée."));
    }

    private Etudiant requireEtudiantParApogee(String apogee) throws SeancesProfesseurException {
        if (!StringUtils.hasText(apogee)) {
            throw new SeancesProfesseurException("Apogée obligatoire.");
        }
        return etudiantRepository.findByApogee(apogee)
                .orElseThrow(() -> new SeancesProfesseurException("Étudiant introuvable (apogée: " + apogee + ")."));
    }

    @Override
    public List<PresenceSortieDTO> saisirPresences(String professeurPpr,Long seanceId, TableauPresencesEntrerDTO dto)
            throws SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        if (dto == null) {
            throw new SeancesProfesseurException("Les données de saisie des présences sont obligatoires.");
        }
        if (dto.getSeanceId() == null) {
            throw new SeancesProfesseurException("SeanceId obligatoire.");
        }
        if (dto.getPresences() == null || dto.getPresences().isEmpty()) {
            throw new SeancesProfesseurException("La liste des présences est obligatoire.");
        }

        // 1) Ownership check
        Seance seance = requireSeancePourProfesseur(professeurPpr, dto.getSeanceId());
            String codeClasse= seance.getAffectationEnseignement().getClasse().getCodeClasse();
        // 2) Upsert ligne par ligne
        for (PresenceEntrerDTO ligne : dto.getPresences()) {

            Etudiant etudiant= requireEtudiantParApogee(ligne.getApogee());

            if(etudiant.getClasse() ==null || !codeClasse.equals(etudiant.getClasse().getCodeClasse())){
                throw new SeancesProfesseurException(
                        "Étudiant non autorisé: l'étudiant n'appartient pas à la classe de cette séance."
                );


            }


            Presence presence = presenceRepository
                    .findBySeance_IdAndEtudiant_Apogee(
                            seance.getId(),
                            etudiant.getApogee()
                    )
                    .orElseGet(Presence::new);

            presence.setSeance(seance);
            presence.setEtudiant(etudiant);
            presence.setStatus(ligne.getStatus());
            presence.setJustifiee(ligne.isJustifiee());
            presence.setMotif(ligne.getMotif());
            presence.setDateDeclaration(LocalDateTime.now());

            presenceRepository.save(presence);
        }

        // 3) Retourner l’état final en DB (pratique pour tester swagger)
        return listerPresencesParSeance(professeurPpr, seance.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresenceSortieDTO> listerPresencesParSeance(String professeurPpr, Long seanceId)
            throws SeancesProfesseurException {

        requireSeancePourProfesseur(professeurPpr, seanceId);

        List<Presence> presences = presenceRepository
                .findBySeance_IdAndSeance_AffectationEnseignement_Professeur_Ppr(seanceId, professeurPpr);

        return presences.stream()
                .map(p -> PresenceSortieDTO.builder()

                        .idPresence(p.getSeance().getId())
                        .apogee(p.getEtudiant().getApogee())
                        .status(p.getStatus())
                        .justifiee(p.isJustifiee())
                        .motif(p.getMotif())
                        .build())
                .toList();
    }

    @Override
    public String TrouverProfesseurPprParemail(String email)throws UtilisateursException {
        Professeur prof= professeurRepository.findByEmail(email).orElseThrow(() ->
                new UtilisateursException("prof introuvable avec l'email: " + email));
         return prof.getPpr();

    }
}
