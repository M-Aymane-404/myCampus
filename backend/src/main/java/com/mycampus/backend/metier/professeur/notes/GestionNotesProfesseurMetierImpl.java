package com.mycampus.backend.metier.professeur.notes;

import com.mycampus.backend.dto.professeur.DonneeEntrer.notes.SaisieNoteEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.notes.LigneNoteSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.notes.PublierNotesSortieDTO;
  /*  import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Examen;
import com.mycampus.backend.entites.Resultat;
import org.springframework.transaction.annotation.Transactional;

public class GestionNotesProfesseurMetierImpl implements GestionNotesProfesseurMetier {
    @Transactional
    @Override
    public LigneNoteSortieDTO upsertNote(String ppr,
                                         Long examenId,
                                         String codeClasse,
                                         String anneeUniversitaire,
                                         SaisieNoteEntrerDTO dto) {

        if (dto == null || dto.getIdEtudiant() == null) throw new NotesProfesseurException("idEtudiant obligatoire.");
        if (dto.getNote() == null) throw new NotesProfesseurException("note obligatoire.");
        if (dto.getNote() < 0 || dto.getNote() > 20) throw new NotesProfesseurException("Note doit être entre 0 et 20.");

        Examen examen = examenRepository.findById(examenId)
                .orElseThrow(() -> new NotesProfesseurException("Examen introuvable."));

        // Ownership (Option B)
        String codeMatiere = examen.getMatiere().getCodeMatiere();
        affectationEnseignementRepository
                .findByActiveTrueAndProfesseur_PprAndClasse_CodeClasseAndMatiere_CodeMatiereAndAnneeUniversitaire(
                        ppr, codeClasse, codeMatiere, anneeUniversitaire
                )
                .orElseThrow(() -> new NotesProfesseurException("Accès refusé: affectation inexistante/inactive."));

        // Étudiant ∈ classe
        if (!etudiantRepository.existsByIdAndClasseCode(dto.getIdEtudiant(), codeClasse)) {
            throw new NotesProfesseurException("Accès refusé: étudiant n'appartient pas à la classe.");
        }

        Etudiant etudiant = etudiantRepository.findById(dto.getIdEtudiant())
                .orElseThrow(() -> new NotesProfesseurException("Étudiant introuvable."));

        Resultat resultat = resultatRepository.findByExamen_IdAndEtudiant_Id(examenId, dto.getIdEtudiant())
                .orElseGet(() -> Resultat.builder()
                        .examen(examen)
                        .etudiant(etudiant)
                        .build());

        // politique simple: si déjà publié => interdit de modifier (tu peux changer plus tard)
        if (resultat.getId() != null && resultat.getDatePublication() != null) {
            throw new NotesProfesseurException("Impossible: note déjà publiée.");
        }

        resultat.setNote(dto.getNote());
        resultat.setDatePublication(null); // autosave brouillon (invisible étudiant)

        Resultat saved = resultatRepository.save(resultat);

        return LigneNoteSortieDTO.builder()
                .idResultat(saved.getId())
                .idEtudiant(etudiant.getId())
                .apogee(etudiant.getApogee())
                .nom(etudiant.getNom())
                .prenom(etudiant.getPrenom())
                .note(saved.getNote())
                .publiee(false)
                .build();
    }

    @Transactional
    @Override
    public PublierNotesSortieDTO publierNotes(String ppr,
                                              Long examenId,
                                              String codeClasse,
                                              String anneeUniversitaire) {

        Examen examen = examenRepository.findById(examenId)
                .orElseThrow(() -> new NotesProfesseurException("Examen introuvable."));

        String codeMatiere = examen.getMatiere().getCodeMatiere();
        affectationEnseignementRepository
                .findByActiveTrueAndProfesseur_PprAndClasse_CodeClasseAndMatiere_CodeMatiereAndAnneeUniversitaire(
                        ppr, codeClasse, codeMatiere, anneeUniversitaire
                )
                .orElseThrow(() -> new NotesProfesseurException("Accès refusé."));

        var resultats = resultatRepository.findByExamenIdWithEtudiant(examenId);
        if (resultats.isEmpty()) throw new NotesProfesseurException("Aucune note à publier.");

        // Option: forcer complétude
        for (Resultat r : resultats) {
            if (r.getNote() == null) throw new NotesProfesseurException("Notes incomplètes.");
            if (r.getDatePublication() != null) throw new NotesProfesseurException("Déjà publié.");
        }

        LocalDateTime now = LocalDateTime.now();
        for (Resultat r : resultats) r.setDatePublication(now);
        resultatRepository.saveAll(resultats);

        examen.setEtat(com.mycampus.backend.enumerations.EtatExamen.CORRIGE);
        examenRepository.save(examen);

        return PublierNotesSortieDTO.builder()
                .examenId(examenId)
                .nbResultatsPublies(resultats.size())
                .build();
    }
}
*/