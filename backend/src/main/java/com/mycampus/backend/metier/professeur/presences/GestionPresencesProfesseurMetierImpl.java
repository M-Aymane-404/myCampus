package com.mycampus.backend.metier.professeur.presences;

import com.mycampus.backend.dto.professeur.DonneeEntrer.presences.PresenceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.EtudiantPresenceSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.FeuilleAppelPresenceSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.JustificatifProfesseurSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.LignePresenceSortieDTO;
import com.mycampus.backend.entites.AffectationEnseignement;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Presence;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.entites.Seance;
import com.mycampus.backend.exceptions.ClasseException;
import com.mycampus.backend.exceptions.PresenceException;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.enumerations.StatutJustificatif;
import com.mycampus.backend.enumerations.StatusAbsence;
import com.mycampus.backend.metier.etudiant.motifDePresence.GestionDeMotifDePresenceMetier;
import com.mycampus.backend.repositorys.AffectationEnseignementRepository;
import com.mycampus.backend.repositorys.ClasseRepository;
import com.mycampus.backend.repositorys.EtudiantRepository;
import com.mycampus.backend.repositorys.PresenceRepository;
import com.mycampus.backend.repositorys.ProfesseurRepository;
import com.mycampus.backend.repositorys.SeanceRepository;
import com.mycampus.backend.metier.security.LienFichierSecuriseService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class GestionPresencesProfesseurMetierImpl implements GestionPresencesProfesseurMetier {

    private final PresenceRepository presenceRepository;
    private final SeanceRepository seanceRepository;
    private final EtudiantRepository etudiantRepository;
    private final ProfesseurRepository professeurRepository;
    private final ClasseRepository classeRepository;
    private final AffectationEnseignementRepository affectationEnseignementRepository;
    private final GestionDeMotifDePresenceMetier gestionDeMotifDePresenceMetier;
    private final LienFichierSecuriseService lienFichierSecuriseService;

    private Professeur requireProfesseurPpr(String professeurPpr) throws SeancesProfesseurException {
        if (!StringUtils.hasText(professeurPpr)) {
            throw new SeancesProfesseurException("Le PPR du professeur est obligatoire.");
        }

        return professeurRepository.findByPpr(professeurPpr)
                .orElseThrow(() -> new SeancesProfesseurException("Professeur introuvable avec ce PPR."));
    }

    private Seance requireSeancePourProfesseur(String professeurPpr, Long seanceId) throws SeancesProfesseurException {
        requireProfesseurPpr(professeurPpr);

        if (seanceId == null) {
            throw new SeancesProfesseurException("SeanceId obligatoire.");
        }

        return seanceRepository
                .findByIdAndAffectationEnseignement_Professeur_Ppr(seanceId, professeurPpr)
                .orElseThrow(() -> new SeancesProfesseurException("Seance introuvable ou non autorisee."));
    }

    private Classe requireClassePourProfesseur(String professeurPpr, String codeClasse) throws ClasseException {
        final Professeur prof;
        try {
            prof = requireProfesseurPpr(professeurPpr);
        } catch (SeancesProfesseurException ex) {
            throw new ClasseException(ex.getMessage());
        }

        if (!StringUtils.hasText(codeClasse)) {
            throw new ClasseException("Code classe obligatoire.");
        }

        Classe classe = classeRepository.findByCodeClasse(codeClasse)
                .orElseThrow(() -> new ClasseException("Classe introuvable ou incorrecte."));

        boolean autorise = affectationEnseignementRepository.existsByProfesseurIdAndClasseId(prof.getId(), classe.getId());
        if (!autorise) {
            throw new ClasseException("Acces refuse : cette classe n'est pas liee a ce professeur.");
        }

        return classe;
    }

    private Etudiant requireEtudiantParApogee(String apogee) throws SeancesProfesseurException {
        if (!StringUtils.hasText(apogee)) {
            throw new SeancesProfesseurException("Apogee obligatoire.");
        }

        return etudiantRepository.findByApogee(apogee)
                .orElseThrow(() -> new SeancesProfesseurException("Etudiant introuvable (apogee: " + apogee + ")."));
    }

    private Presence requirePresencePourSeance(String professeurPpr, Long seanceId, Long presenceId)
            throws SeancesProfesseurException {
        requireSeancePourProfesseur(professeurPpr, seanceId);

        Presence presence = presenceRepository.findById(presenceId)
                .orElseThrow(() -> new SeancesProfesseurException("Presence introuvable."));

        if (presence.getSeance() == null || !Objects.equals(presence.getSeance().getId(), seanceId)) {
            throw new SeancesProfesseurException("La presence ne correspond pas a cette seance.");
        }

        return presence;
    }

    private boolean hasJustification(Presence presence) {
        return presence != null && (
                StringUtils.hasText(presence.getDescriptionDeJustification())
                        || StringUtils.hasText(presence.getMotifUrl())
                        || StringUtils.hasText(presence.getMotifNom())
                        || presence.getTypeMotif() != null
        );
    }

    private StatutJustificatif resolveStatutJustificatif(Presence presence) {
        if (presence == null) {
            return StatutJustificatif.AUCUN;
        }

        if (presence.getStatutJustificatif() != null) {
            return presence.getStatutJustificatif();
        }

        return hasJustification(presence) ? StatutJustificatif.EN_ATTENTE : StatutJustificatif.AUCUN;
    }

    private LignePresenceSortieDTO toLignePresenceSortieDTO(Presence presence, Etudiant etudiant) {
        boolean justificationSoumise = hasJustification(presence);
        StatutJustificatif statutJustificatif = resolveStatutJustificatif(presence);

        return LignePresenceSortieDTO.builder()
                .idPresence(presence != null ? presence.getId() : null)
                .idEtudiant(etudiant != null ? etudiant.getId() : null)
                .apogee(etudiant != null ? etudiant.getApogee() : null)
                .nomEtudiant(etudiant != null ? etudiant.getNom() : null)
                .prenomEtudiant(etudiant != null ? etudiant.getPrenom() : null)
                .status(presence != null ? presence.getStatus() : null)
                .statutJustificatif(statutJustificatif)
                .justifiee(statutJustificatif == StatutJustificatif.VALIDE)
                .justificationSoumise(justificationSoumise)
                .motif(presence != null ? presence.getDescriptionDeJustification() : null)
                .typeMotif(presence != null ? presence.getTypeMotif() : null)
                .motifUrl(presence != null ? lienFichierSecuriseService.construireUrlJustificatif(presence.getId(), presence.getMotifNom()) : null)
                .motifNom(presence != null ? presence.getMotifNom() : null)
                .dateDeclaration(presence != null ? presence.getDateDeclaration() : null)
                .build();
    }

    private JustificatifProfesseurSortieDTO toJustificatifProfesseurSortieDTO(Presence presence) {
        if (presence == null || presence.getEtudiant() == null || presence.getSeance() == null) {
            return null;
        }

        Seance seance = presence.getSeance();
        AffectationEnseignement affectation = seance.getAffectationEnseignement();
        Etudiant etudiant = presence.getEtudiant();
        StatutJustificatif statutJustificatif = resolveStatutJustificatif(presence);
        boolean justificationSoumise = hasJustification(presence);

        return JustificatifProfesseurSortieDTO.builder()
                .presenceId(presence.getId())
                .seanceId(seance.getId())
                .idEtudiant(etudiant.getId())
                .apogee(etudiant.getApogee())
                .nomEtudiant(etudiant.getNom())
                .prenomEtudiant(etudiant.getPrenom())
                .codeClasse(affectation != null && affectation.getClasse() != null ? affectation.getClasse().getCodeClasse() : null)
                .nomClasse(affectation != null && affectation.getClasse() != null ? affectation.getClasse().getNom() : null)
                .codeMatiere(affectation != null && affectation.getMatiere() != null ? affectation.getMatiere().getCodeMatiere() : null)
                .nomMatiere(affectation != null && affectation.getMatiere() != null ? affectation.getMatiere().getNom() : null)
                .typeSeance(seance.getType())
                .dateAbsence(seance.getDate())
                .heureDebut(seance.getHeureDebut())
                .heureFin(seance.getHeureFin())
                .salle(seance.getSalle())
                .status(presence.getStatus())
                .statutJustificatif(statutJustificatif)
                .justifiee(statutJustificatif == StatutJustificatif.VALIDE)
                .justificationSoumise(justificationSoumise)
                .motif(presence.getDescriptionDeJustification())
                .typeMotif(presence.getTypeMotif())
                .motifUrl(lienFichierSecuriseService.construireUrlJustificatif(presence.getId(), presence.getMotifNom()))
                .motifNom(presence.getMotifNom())
                .dateDeclaration(presence.getDateDeclaration())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<EtudiantPresenceSortieDTO> listerEtudiantsparCodeClasse(String professeurPpr, String codeClasse)
            throws UtilisateursException, SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        final Classe classe;
        try {
            classe = requireClassePourProfesseur(professeurPpr, codeClasse);
        } catch (ClasseException ex) {
            throw new UtilisateursException(ex.getMessage());
        }

        List<Etudiant> etudiants = etudiantRepository.findByClasseId(classe.getId());

        return etudiants.stream()
                .map(e -> new EtudiantPresenceSortieDTO(e.getId(), e.getNom(), e.getPrenom(), e.getApogee()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FeuilleAppelPresenceSortieDTO chargerFeuilleAppelPresence(String professeurPpr, Long seanceId)
            throws SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        Seance seance = requireSeancePourProfesseur(professeurPpr, seanceId);

        Long classeId = seance.getAffectationEnseignement().getClasse().getId();
        List<Etudiant> etudiants = etudiantRepository.findByClasseId(classeId);
        List<Presence> presences = presenceRepository
                .findBySeance_IdAndSeance_AffectationEnseignement_Professeur_Ppr(seanceId, professeurPpr);

        Map<Long, LignePresenceSortieDTO> parIdEtudiant = new HashMap<>();
        for (Presence presence : presences) {
            Etudiant etudiant = presence.getEtudiant();
            parIdEtudiant.put(etudiant.getId(), toLignePresenceSortieDTO(presence, etudiant));
        }

        List<LignePresenceSortieDTO> lignesFinales = new ArrayList<>();
        for (Etudiant etudiant : etudiants) {
            LignePresenceSortieDTO ligne = parIdEtudiant.get(etudiant.getId());
            if (ligne != null) {
                lignesFinales.add(ligne);
            } else {
                lignesFinales.add(toLignePresenceSortieDTO(null, etudiant));
            }
        }

        return FeuilleAppelPresenceSortieDTO.builder()
                .seanceId(seance.getId())
                .date(seance.getDate())
                .heureDebut(seance.getHeureDebut())
                .heureFin(seance.getHeureFin())
                .salle(seance.getSalle())
                .codeClasse(seance.getAffectationEnseignement().getClasse().getCodeClasse())
                .nomMatiere(seance.getAffectationEnseignement().getMatiere().getNom())
                .lignes(lignesFinales)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JustificatifProfesseurSortieDTO> listerJustificatifsProfesseur(
            String professeurPpr,
            StatutJustificatif statutJustificatif,
            String codeClasse,
            String recherche,
            LocalDate dateDebut,
            LocalDate dateFin
    ) throws SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        final String rechercheNormalisee = StringUtils.hasText(recherche) ? recherche.trim().toLowerCase() : null;
        final String codeClasseNormalise = StringUtils.hasText(codeClasse) ? codeClasse.trim() : null;

        return presenceRepository.listerJustificatifsParProfesseur(professeurPpr).stream()
                .filter(this::hasJustification)
                .filter(presence -> statutJustificatif == null || resolveStatutJustificatif(presence) == statutJustificatif)
                .filter(presence -> {
                    if (codeClasseNormalise == null) {
                        return true;
                    }

                    Seance seance = presence.getSeance();
                    AffectationEnseignement affectation = seance != null ? seance.getAffectationEnseignement() : null;
                    Classe classe = affectation != null ? affectation.getClasse() : null;
                    return classe != null && codeClasseNormalise.equalsIgnoreCase(classe.getCodeClasse());
                })
                .filter(presence -> {
                    if (dateDebut == null && dateFin == null) {
                        return true;
                    }

                    LocalDate dateAbsence = presence.getSeance() != null ? presence.getSeance().getDate() : null;
                    if (dateAbsence == null) {
                        return false;
                    }

                    boolean afterStart = dateDebut == null || !dateAbsence.isBefore(dateDebut);
                    boolean beforeEnd = dateFin == null || !dateAbsence.isAfter(dateFin);
                    return afterStart && beforeEnd;
                })
                .filter(presence -> {
                    if (rechercheNormalisee == null) {
                        return true;
                    }

                    Etudiant etudiant = presence.getEtudiant();
                    String prenom = etudiant != null && etudiant.getPrenom() != null ? etudiant.getPrenom().toLowerCase() : "";
                    String nom = etudiant != null && etudiant.getNom() != null ? etudiant.getNom().toLowerCase() : "";
                    String apogee = etudiant != null && etudiant.getApogee() != null ? etudiant.getApogee().toLowerCase() : "";
                    String nomComplet = (prenom + " " + nom).trim();
                    String nomInverse = (nom + " " + prenom).trim();

                    return prenom.contains(rechercheNormalisee)
                            || nom.contains(rechercheNormalisee)
                            || apogee.contains(rechercheNormalisee)
                            || nomComplet.contains(rechercheNormalisee)
                            || nomInverse.contains(rechercheNormalisee);
                })
                .map(this::toJustificatifProfesseurSortieDTO)
                .filter(Objects::nonNull)
                .sorted(
                        Comparator
                                .comparingInt((JustificatifProfesseurSortieDTO dto) -> switch (dto.getStatutJustificatif()) {
                                    case EN_ATTENTE -> 0;
                                    case VALIDE -> 1;
                                    case REFUSE -> 2;
                                    default -> 3;
                                })
                                .thenComparing(JustificatifProfesseurSortieDTO::getDateAbsence, Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(JustificatifProfesseurSortieDTO::getHeureDebut, Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .toList();
    }

    @Override
    public LignePresenceSortieDTO saisirPresences(String professeurPpr, Long seanceId, PresenceEntrerDTO dto)
            throws SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        if (dto == null) {
            throw new SeancesProfesseurException("Les donnees de saisie des presences sont obligatoires.");
        }
        if (!StringUtils.hasText(dto.getApogee())) {
            throw new SeancesProfesseurException("Apogee obligatoire.");
        }
        if (dto.getStatus() == null) {
            throw new SeancesProfesseurException("Statut obligatoire.");
        }

        Etudiant etudiant = requireEtudiantParApogee(dto.getApogee());
        Seance seance = requireSeancePourProfesseur(professeurPpr, seanceId);

        boolean appartient = etudiantRepository.TrouverEtudiantExistenClasse(seanceId, etudiant.getId());
        if (!appartient) {
            throw new SeancesProfesseurException("L'etudiant n'appartient pas a la classe de cette seance.");
        }

        Presence presence = presenceRepository
                .findBySeance_IdAndEtudiant_Apogee(seanceId, dto.getApogee())
                .orElseGet(() -> Presence.builder()
                        .etudiant(etudiant)
                        .seance(seance)
                        .build());

        presence.setStatus(dto.getStatus());
        presence.setDateDeclaration(LocalDateTime.now());
        if (dto.getStatus() == StatusAbsence.PRESENT) {
            presence.setDescriptionDeJustification(null);
            presence.setTypeMotif(null);
            presence.setMotifUrl(null);
            presence.setMotifNom(null);
            presence.setStatutJustificatif(StatutJustificatif.AUCUN);
        }

        Presence saved = presenceRepository.save(presence);
        return toLignePresenceSortieDTO(saved, etudiant);
    }

    @Override
    public LignePresenceSortieDTO validerJustificatif(String professeurPpr, Long seanceId, Long presenceId)
            throws SeancesProfesseurException {
        Presence presence = requirePresencePourSeance(professeurPpr, seanceId, presenceId);

        boolean justificationSoumise =
                StringUtils.hasText(presence.getDescriptionDeJustification())
                        || StringUtils.hasText(presence.getMotifUrl())
                        || StringUtils.hasText(presence.getMotifNom())
                        || presence.getTypeMotif() != null;

        if (!justificationSoumise) {
            throw new SeancesProfesseurException("Aucun justificatif soumis pour cette absence.");
        }

        presence.setStatutJustificatif(StatutJustificatif.VALIDE);
        Presence saved = presenceRepository.save(presence);
        return toLignePresenceSortieDTO(saved, saved.getEtudiant());
    }

    @Override
    public LignePresenceSortieDTO refuserJustificatif(String professeurPpr, Long seanceId, Long presenceId)
            throws SeancesProfesseurException {
        Presence presence = requirePresencePourSeance(professeurPpr, seanceId, presenceId);

        boolean justificationSoumise =
                StringUtils.hasText(presence.getDescriptionDeJustification())
                        || StringUtils.hasText(presence.getMotifUrl())
                        || StringUtils.hasText(presence.getMotifNom())
                        || presence.getTypeMotif() != null;

        if (!justificationSoumise) {
            throw new SeancesProfesseurException("Aucun justificatif soumis pour cette absence.");
        }

        presence.setStatutJustificatif(StatutJustificatif.REFUSE);

        Presence saved = presenceRepository.save(presence);
        return toLignePresenceSortieDTO(saved, saved.getEtudiant());
    }

    @Override
    @Transactional(readOnly = true)
    public Resource telechargerJustificatif(String professeurPpr, Long seanceId, Long presenceId) throws SeancesProfesseurException {
        Presence presence = requirePresencePourSeance(professeurPpr, seanceId, presenceId);
        String motifNom = presence.getMotifNom();
        if (!StringUtils.hasText(motifNom)) {
            throw new SeancesProfesseurException("Aucun justificatif enregistré pour cette présence.");
        }

        try {
            return gestionDeMotifDePresenceMetier.chargerMotifDePresence(motifNom);
        } catch (PresenceException ex) {
            throw new SeancesProfesseurException(ex.getMessage(), ex);
        }
    }
}
