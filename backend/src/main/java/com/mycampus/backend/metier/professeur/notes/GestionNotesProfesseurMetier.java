package com.mycampus.backend.metier.professeur.notes;

import com.mycampus.backend.dto.professeur.DonneeEntrer.notes.SaisieNoteEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.notes.LigneNoteSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.notes.PublierNotesSortieDTO;

public interface GestionNotesProfesseurMetier {
    LigneNoteSortieDTO upsertNote(String ppr,
                                  Long examenId,
                                  String codeClasse,
                                  String anneeUniversitaire,
                                  SaisieNoteEntrerDTO dto);

    PublierNotesSortieDTO publierNotes(String ppr,
                                       Long examenId,
                                       String codeClasse,
                                       String anneeUniversitaire);
}

