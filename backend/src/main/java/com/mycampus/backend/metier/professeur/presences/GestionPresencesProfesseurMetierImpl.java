package com.mycampus.backend.metier.professeur.presences;

import com.mycampus.backend.dto.professeur.DonneeEntrer.presences.PresenceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.EtudiantPresenceSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.LignePresenceSortieDTO;
import com.mycampus.backend.entites.*;
import com.mycampus.backend.exceptions.ClasseException;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.mappers.professeur.PresenceMapper;
import com.mycampus.backend.repositorys.*;
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
    private final PresenceMapper presenceMapper;
    private final ClasseRepository classeRepository;

    private Professeur requireProfesseurPpr(String professeurPpr) throws SeancesProfesseurException {
        if (!StringUtils.hasText(professeurPpr)) {
            throw new SeancesProfesseurException("Le PPR du professeur est obligatoire.");
        }
        return  professeurRepository.findByPpr(professeurPpr)
                .orElseThrow(()-> new SeancesProfesseurException("professeur  introuvable  avec ce Ppr"));
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
    private Classe requireClassePourProfesseur(String professeurPpr, String codeClasse) throws ClasseException {
        requireProfesseurPpr(professeurPpr);

        if (codeClasse==null){
            throw new ClasseException("code Classe obligatoire.");
        }
;
        return classeRepository.findByCodeClasse(codeClasse)
                .orElseThrow(() -> new ClasseException("Classe introuvable ou  incorrect."));


    }

    private Etudiant requireEtudiantParApogee(String apogee) throws SeancesProfesseurException {
        if (!StringUtils.hasText(apogee)) {
            throw new SeancesProfesseurException("Apogée obligatoire.");
        }
        return etudiantRepository.findByApogee(apogee)
                .orElseThrow(() -> new SeancesProfesseurException("Étudiant introuvable (apogée: " + apogee + ")."));
    }

    public List<EtudiantPresenceSortieDTO> listerEtudiantsparCodeClasse(String professeurPpr, String codeClasse )
        throws UtilisateursException, SeancesProfesseurException {
        Professeur prof= requireProfesseurPpr(professeurPpr);

    }


 
    @Override
    public LignePresenceSortieDTO saisirPresences(String professeurPpr, Long seanceId, PresenceEntrerDTO dto)
            throws SeancesProfesseurException {

        Professeur prof = requireProfesseurPpr(professeurPpr);

        if (dto == null) {
            throw new SeancesProfesseurException("Les données de saisie des présences sont obligatoires.");
        }
        if (dto.getApogee() == null || dto.getApogee().isBlank()) {
            throw new SeancesProfesseurException("Apogée obligatoire.");
        }

        Etudiant etudiant = requireEtudiantParApogee(dto.getApogee());

        // 1) Ownership check via PATH seanceId
        Seance seance = requireSeancePourProfesseur(professeurPpr, seanceId);
        boolean existe = etudiantRepository.TrouverEtudiantExistenClasse(seance.getId(), etudiant.getId());
        if (!existe) {
            throw new SeancesProfesseurException("etudiant n'appartient pas à cette classe ");
        }

        Presence presence = Presence.builder()
                .etudiant(etudiant)
                .seance(seance)
                .dateDeclaration(LocalDateTime.now())
                .status(dto.getStatus())
                .build();

        presenceRepository.save(presence);
        return presenceMapper.FromPresencetoPresencceSortieDTO(presence) ;
    }


    @Override
    public String TrouverProfesseurPprParemail(String email) throws UtilisateursException {
        Professeur prof = professeurRepository.findByEmail(email).orElseThrow(() ->
                new UtilisateursException("prof introuvable avec l'email: " + email));
        return prof.getPpr();
    }
}
    /*
    @Override
    @Transactional(readOnly = true)
    public List<PresenceSortieDTO> listerPresencesParSeance(String professeurPpr, Long seanceId)
            throws SeancesProfesseurException {

        Seance seance = requireSeancePourProfesseur(professeurPpr, seanceId);

        Long classeId = seance.getAffectationEnseignement().getClasse().getId();
        if (classeId == null) {
            throw new SeancesProfesseurException("Classe introuvable pour cette séance.");
        }

        // 1) Tous les étudiants de la classe
        // -> Il faut un repo method : findByClasse_Id(classeId)
        List<Etudiant> etudiants = etudiantRepository.findByClasse_Id(classeId);

        // 2) Les présences déjà saisies pour la séance
        List<Presence> presences = presenceRepository
                .findBySeance_IdAndSeance_AffectationEnseignement_Professeur_Ppr(seanceId, professeurPpr);

        Map<String, Presence> presenceByApogee = presences.stream()
                .filter(p -> p.getEtudiant() != null && StringUtils.hasText(p.getEtudiant().getApogee()))
                .collect(Collectors.toMap(
                        p -> p.getEtudiant().getApogee(),
                        Function.identity(),
                        (a, b) -> a // si doublon, garde le premier (normalement pas de doublon)
                ));

        // 3) Merge : pour chaque étudiant → status existant sinon défaut
        // Choisis ton défaut : PRESENT (souvent) ou ABSENT.
        // Je mets PRESENT par défaut (tu peux changer).
        return etudiants.stream()
                .map(e -> {
                    Presence p = presenceByApogee.get(e.getApogee());

                    return PresenceSortieDTO.builder()
                            .idPresence(p != null ? p.getId() : null)
                            .apogee(e.getApogee())
                            .status(p != null ? p.getStatus() : com.mycampus.backend.entites.enums.StatusPresence.PRESENT)
                            .justifiee(p != null && p.isJustifiee())
                            .motif(p != null ? p.getMotif() : null)
                            .build();
                })
                .toList();
    }
*/


