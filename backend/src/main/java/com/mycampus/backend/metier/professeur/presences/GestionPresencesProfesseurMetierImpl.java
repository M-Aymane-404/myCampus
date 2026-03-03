// src/main/java/com/mycampus/backend/metier/professeur/presences/GestionPresencesProfesseurMetierImpl.java
package com.mycampus.backend.metier.professeur.presences;

import com.mycampus.backend.dto.professeur.DonneeEntrer.presences.PresenceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.EtudiantPresenceSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.FeuilleAppelPresenceSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.LignePresenceSortieDTO;
import com.mycampus.backend.entites.*;
import com.mycampus.backend.exceptions.ClasseException;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.repositorys.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

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
                .orElseThrow(() -> new SeancesProfesseurException("Séance introuvable ou non autorisée."));
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
            throw new ClasseException("Accès refusé : cette classe n'est pas liée à ce professeur.");
        }

        return classe;
    }

    private Etudiant requireEtudiantParApogee(String apogee) throws SeancesProfesseurException {
        if (!StringUtils.hasText(apogee)) {
            throw new SeancesProfesseurException("Apogée obligatoire.");
        }
        return etudiantRepository.findByApogee(apogee)
                .orElseThrow(() -> new SeancesProfesseurException("Étudiant introuvable (apogée: " + apogee + ")."));
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

        // EntityGraph => p.getEtudiant() is loaded safely
        List<Presence> presences = presenceRepository
                .findBySeance_IdAndSeance_AffectationEnseignement_Professeur_Ppr(seanceId, professeurPpr);

        Map<Long, LignePresenceSortieDTO> parIdEtudiant = new HashMap<>();
        for (Presence p : presences) {
            Etudiant e = p.getEtudiant();
            parIdEtudiant.put(
                    e.getId(),
                    LignePresenceSortieDTO.builder()
                            .idPresence(p.getId())
                            .idEtudiant(e.getId())
                            .apogee(e.getApogee())
                            .nomEtudiant(e.getNom())
                            .prenomEtudiant(e.getPrenom())
                            .status(p.getStatus())
                            .justifiee(p.isValidationMotif())
                            .motif(p.getDescriptionDeJustification())
                            .dateDeclaration(p.getDateDeclaration())
                            .build()
            );
        }

        List<LignePresenceSortieDTO> lignesFinales = new ArrayList<>();
        for (Etudiant e : etudiants) {
            LignePresenceSortieDTO ligne = parIdEtudiant.get(e.getId());
            if (ligne != null) {
                lignesFinales.add(ligne);
            } else {
                lignesFinales.add(
                        LignePresenceSortieDTO.builder()
                                .idPresence(null)
                                .idEtudiant(e.getId())
                                .apogee(e.getApogee())
                                .nomEtudiant(e.getNom())
                                .prenomEtudiant(e.getPrenom())
                                .status(null) // non saisi
                                .justifiee(false)
                                .motif(null)
                                .dateDeclaration(null)
                                .build()
                );
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
    public LignePresenceSortieDTO saisirPresences(String professeurPpr, Long seanceId, PresenceEntrerDTO dto)
            throws SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        if (dto == null) {
            throw new SeancesProfesseurException("Les données de saisie des présences sont obligatoires.");
        }
        if (!StringUtils.hasText(dto.getApogee())) {
            throw new SeancesProfesseurException("Apogée obligatoire.");
        }
        if (dto.getStatus() == null) {
            throw new SeancesProfesseurException("Statut obligatoire.");
        }

        Etudiant etudiant = requireEtudiantParApogee(dto.getApogee());
        Seance seance = requireSeancePourProfesseur(professeurPpr, seanceId);

        boolean appartient = etudiantRepository.TrouverEtudiantExistenClasse(seanceId, etudiant.getId());
        if (!appartient) {
            throw new SeancesProfesseurException("L'étudiant n'appartient pas à la classe de cette séance.");
        }

        // UPSERT (no duplicates)
        Presence presence = presenceRepository
                .findBySeance_IdAndEtudiant_Apogee(seanceId, dto.getApogee())
                .orElseGet(() -> Presence.builder()
                        .etudiant(etudiant)
                        .seance(seance)
                        .build());

        presence.setStatus(dto.getStatus());
        presence.setDateDeclaration(LocalDateTime.now());
        // PresenceEntrerDTO has only apogee + status => we do NOT touch justification fields here

        Presence saved = presenceRepository.save(presence);

        return LignePresenceSortieDTO.builder()
                .idPresence(saved.getId())
                .idEtudiant(etudiant.getId())
                .apogee(etudiant.getApogee())
                .nomEtudiant(etudiant.getNom())
                .prenomEtudiant(etudiant.getPrenom())
                .status(saved.getStatus())
                .justifiee(saved.isValidationMotif())
                .motif(saved.getDescriptionDeJustification())
                .dateDeclaration(saved.getDateDeclaration())
                .build();
    }

   /* @Override
    @Transactional(readOnly = true)
    public String TrouverProfesseurPprParemail(String email) throws UtilisateursException {
        Professeur prof = professeurRepository.findByEmail(email)
                .orElseThrow(() -> new UtilisateursException("Prof introuvable avec l'email: " + email));
        return prof.getPpr();
    }*/
}