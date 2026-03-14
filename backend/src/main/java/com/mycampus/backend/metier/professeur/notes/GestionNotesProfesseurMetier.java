package com.mycampus.backend.metier.professeur.notes;

import com.mycampus.backend.dto.professeur.DonneeEntrer.notes.SaisieNoteEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.examens.ExamenSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.notes.FeuilleNotesSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.notes.LigneNoteSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.notes.PublierNotesSortieDTO;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface GestionNotesProfesseurMetier {

    FeuilleNotesSortieDTO chargerFeuilleNotes(String ppr, String codeClasse, Long examenId );

    LigneNoteSortieDTO autosaveNote(String ppr, String codeClasse, Long examenId, SaisieNoteEntrerDTO dto);



    int publierNotes(String ppr, String codeClasse, Long examenId);


    LigneNoteSortieDTO deposerCopieCorrigeeEtudiant(String ppr, String codeClasse, Long examenId, Long idEtudiant, MultipartFile pdf);

    @Transactional(readOnly = true)
    Resource telechargerCopieCorrigeeEtudiantProf(String ppr, String codeClasse, Long examenId, Long idEtudiant);


     //LigneNoteSortieDTO deposerCopieCorrigeeEtudiant(String ppr, String codeClasse, Long examenId, Long etudiantId, MultipartFile pdf);

   // Resource telechargerCopieEtudiantProf(String ppr, String codeClasse, Long examenId, Long etudiantId);
}

