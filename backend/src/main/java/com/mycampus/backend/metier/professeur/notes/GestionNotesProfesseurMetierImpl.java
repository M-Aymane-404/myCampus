package com.mycampus.backend.metier.professeur.notes;

import com.mycampus.backend.dto.professeur.DonneeEntrer.notes.SaisieNoteEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.notes.FeuilleNotesSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.notes.LigneNoteSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.notes.StatsNotesSortieDTO;
import com.mycampus.backend.entites.*;
import com.mycampus.backend.enumerations.EtatExamen;
import com.mycampus.backend.exceptions.NotesProfesseurException;
import com.mycampus.backend.mappers.professeur.NotesProfesseurMapper;
import com.mycampus.backend.repositorys.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class GestionNotesProfesseurMetierImpl implements GestionNotesProfesseurMetier {


    private final ExamenRepository examenRepo;
    private final ClasseRepository classeRepo;
    private final EtudiantRepository etudiantRepo;
    private final ResultatRepository resultatRepo;
    private final AffectationEnseignementRepository affectationRepo;
    private final NotesProfesseurMapper notesProfesseurMapper;

    private Examen requireExamen(Long examenId) {
        return examenRepo.findByIdWithMatiere(examenId)
                .orElseThrow(() -> new NotesProfesseurException("Examen introuvable."));
    }

    private void requireOwnership(String ppr, String codeClasse, String codeMatiere) {
        List<AffectationEnseignement> aes =
                affectationRepo.findActiveByProfClasseMatiere(ppr, codeClasse, codeMatiere);

        if (aes.isEmpty()) {
            throw new NotesProfesseurException("Accès refusé: affectation inexistante ou inactive.");
        }
    }

    private Classe requireClasse(String codeClasse) {
        return classeRepo.findByCodeClasse(codeClasse)
                .orElseThrow(() -> new NotesProfesseurException("Classe introuvable: " + codeClasse));
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    @Override
    @Transactional(readOnly = true)
    public FeuilleNotesSortieDTO chargerFeuilleNotes(String ppr, String codeClasse, Long examenId) {
        Examen examen = requireExamen(examenId);
        String codeMatiere = examen.getMatiere().getCodeMatiere();

        requireOwnership(ppr, codeClasse, codeMatiere);
        Classe classe = requireClasse(codeClasse);

        List<Etudiant> etudiants =
                etudiantRepo.findByClasse_CodeClasseOrderByNomAscPrenomAsc(codeClasse);

        List<Resultat> resultats = resultatRepo.findAllByExamenId(examenId);

        Map<Long, Resultat> mapRes = resultats.stream()
                .filter(r -> r.getEtudiant() != null)
                .collect(Collectors.toMap(r -> r.getEtudiant().getId(), r -> r, (a, b) -> a));

        List<LigneNoteSortieDTO> lignes = new ArrayList<>();
        List<Double> notes = new ArrayList<>();
        int nbSaisies = 0;

        for (Etudiant etudiant : etudiants) {
            Resultat resultat = mapRes.get(etudiant.getId());

            LigneNoteSortieDTO ligne = notesProfesseurMapper.toLigneDTO(etudiant, resultat);
            lignes.add(ligne);

            if (ligne.getNote() != null) {
                nbSaisies++;
                notes.add(ligne.getNote());
            }
        }

        StatsNotesSortieDTO stats = buildStats(etudiants.size(), nbSaisies, notes);

        return FeuilleNotesSortieDTO.builder()
                .codeClasse(codeClasse)
                .nomClasse(classe.getNom())
                .idExamen(examen.getId())
                .codeMatiere(codeMatiere)
                .nomMatiere(examen.getMatiere().getNom())
                .semestre(examen.getSemestre())
                .numeroExamen(examen.getNumeroExamen())
                .typeExamen(examen.getTypeExamen())
                .dateExamen(examen.getDateExamen())
                .duree(examen.getDuree())
                .etat(examen.getEtat())
                .lignes(lignes)
                .stats(stats)
                .build();
    }

    private StatsNotesSortieDTO buildStats(int total, int nbSaisies, List<Double> notes) {
        Double min = null;
        Double max = null;
        Double moy = null;

        if (!notes.isEmpty()) {
            min = notes.stream().min(Double::compareTo).orElse(null);
            max = notes.stream().max(Double::compareTo).orElse(null);
            moy = round2(notes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
        }

        return StatsNotesSortieDTO.builder()
                .totalEtudiants(total)
                .nbNotesSaisies(nbSaisies)
                .min(min)
                .max(max)
                .moyenne(moy)
                .build();
    }

    @Override

    public LigneNoteSortieDTO autosaveNote(String ppr, String codeClasse, Long examenId, SaisieNoteEntrerDTO dto) {
        if (dto == null || dto.getIdEtudiant() == null) {
            throw new NotesProfesseurException("idEtudiant obligatoire.");
        }

        if (dto.getNote() != null && (dto.getNote() < 0 || dto.getNote() > 20)) {
            throw new NotesProfesseurException("La note doit être entre 0 et 20.");
        }

        Examen examen = requireExamen(examenId);
        String codeMatiere = examen.getMatiere().getCodeMatiere();
        requireOwnership(ppr, codeClasse, codeMatiere);

        Etudiant etudiant = etudiantRepo.findById(dto.getIdEtudiant())
                .orElseThrow(() -> new NotesProfesseurException("Étudiant introuvable."));

        if (!etudiantRepo.existsByIdAndClasse_CodeClasse(dto.getIdEtudiant(), codeClasse)) {
            throw new NotesProfesseurException("Étudiant ne fait pas partie de la classe.");
        }

        Resultat resultat = resultatRepo.findByExamen_IdAndEtudiant_Id(examenId, dto.getIdEtudiant())
                .orElseGet(() -> {
                    Resultat r = new Resultat();
                    r.setExamen(examen);
                    r.setEtudiant(etudiant);
                    return r;
                });

        resultat.setNote(dto.getNote());
        resultat.setDatePublication(null);

        Resultat saved = resultatRepo.save(resultat);

        return notesProfesseurMapper.toLigneDTO(etudiant, saved);
    }

    @Override


    public int publierNotes(String ppr, String codeClasse, Long examenId) {
        Examen examen = requireExamen(examenId);
        String codeMatiere = examen.getMatiere().getCodeMatiere();
        requireOwnership(ppr, codeClasse, codeMatiere);

        List<Resultat> resultats = resultatRepo.findAllByExamenId(examenId);

        if (resultats.isEmpty()) {
            throw new NotesProfesseurException("Aucun résultat à publier.");
        }

        LocalDateTime now = LocalDateTime.now();

        for (Resultat r : resultats) {
            if (r.getNote() != null) {
                r.setDatePublication(now);
            }
        }

        resultatRepo.saveAll(resultats);

        examen.setEtat(EtatExamen.CORRIGE);
        examenRepo.save(examen);

        return (int) resultats.stream()
                .filter(r -> r.getDatePublication() != null)
                .count();
    }
}
