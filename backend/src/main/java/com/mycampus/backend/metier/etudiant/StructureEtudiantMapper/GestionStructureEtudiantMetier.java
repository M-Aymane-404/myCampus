package com.mycampus.backend.metier.etudiant.StructureEtudiantMapper;

import com.mycampus.backend.dto.etudiant.DonneeSortie.*;
import com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote.ReleveDeNoteEtudiantParSessionDTO;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.StatusAbsence;
import com.mycampus.backend.enumerations.TypeSeance;
import com.mycampus.backend.exceptions.EtudiantException;

import java.time.LocalDate;
import java.util.List;

public interface GestionStructureEtudiantMetier {

    EtudiantSortieEtudiantDTO trouverEtudiantConnecter(Long id);

    List<ModuleSortieEtudiantDTO> trouverModulesDEtudiantConnecter(Long id) throws EtudiantException;

    List<ModuleSortieEtudiantDTO> trouverModulesDEtudiantConnecterParSemester(Long id, Semestre semestre) throws EtudiantException;

    List<MatiereSortieEtudiantDTO> trouverMatiereParCodeModule(Long EtudiantId , String codeModule) throws EtudiantException;

    List<SeanceSortieEtudiantDTO> trouverSeanceParCodeMatiereEtFiltrerParTypeSeanceSiExist(Long etudiantId, String codeMatiere, TypeSeance typeSeance) throws EtudiantException;

    List<FichierSeanceSortieEtudiantDTO> trouverRessourceD_uneSeance(Long seanceId);

    List<PresenceSortieEtudiantDTO> trouverToutPresenceDuEtudiantConnecter(Long IdEtudiant, StatusAbsence status, LocalDate date) throws EtudiantException;

    ReleveDeNoteEtudiantParSessionDTO trouverLeReleverDeNoteParSessionDuEtudiant(Long idEtudiant, Semestre semestre)
            throws EtudiantException;
}
