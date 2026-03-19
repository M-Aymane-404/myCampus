package com.mycampus.backend.metier.etudiant.StructureEtudiant;

import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.EtudiantModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.EtudiantSortieDTO;
import com.mycampus.backend.dto.etudiant.DonneeEntrer.EtudiantModificationEtudiantDTO;
import com.mycampus.backend.dto.etudiant.DonneeSortie.*;
import com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote.ReleveDeNoteEtudiantParSessionDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.fichiersSeance.TelechargementFichierSeanceSortieDTO;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.StatusAbsence;
import com.mycampus.backend.enumerations.TypeSeance;
import com.mycampus.backend.exceptions.EtudiantException;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.exceptions.UtilisateursException;
import org.springframework.core.io.Resource;

import java.time.LocalDate;
import java.util.List;

public interface GestionStructureEtudiantMetier {

    EtudiantSortieEtudiantDTO trouverEtudiantConnecter(Long id);

    public EtudiantSortieEtudiantDTO modifierEtudiant(String email, EtudiantModificationEtudiantDTO etudiantModificationDTO) throws UtilisateursException;
    List<ModuleSortieEtudiantDTO> trouverModulesDEtudiantConnecter(Long id) throws EtudiantException;

    List<ModuleSortieEtudiantDTO> trouverModulesDEtudiantConnecterParSemester(Long id, Semestre semestre) throws EtudiantException;

    List<MatiereSortieEtudiantDTO> trouverMatiereParCodeModule(Long EtudiantId , String codeModule) throws EtudiantException;

    List<SeanceSortieEtudiantDTO> trouverSeanceParCodeMatiereEtFiltrerParTypeSeanceSiExist(Long etudiantId, String codeMatiere, TypeSeance typeSeance) throws EtudiantException;

    List<FichierSeanceSortieEtudiantDTO> trouverRessourceD_uneSeance(Long seanceId);

    List<PresenceSortieEtudiantDTO> trouverToutPresenceDuEtudiantConnecter(Long IdEtudiant, StatusAbsence status, LocalDate date) throws EtudiantException;

    ReleveDeNoteEtudiantParSessionDTO trouverLeReleverDeNoteParSessionDuEtudiant(Long idEtudiant, Semestre semestre)
            throws EtudiantException;
    TelechargementFichierSeanceSortieDTO preparerTelechargementFichierSeance(
            Long idEtudiant,
            Long fichierSeanceId
    ) throws SeancesProfesseurException;

    Resource telechargerCopieExamen(Long idEtudiant, Long examenId) throws EtudiantException;

    Resource telechargerCorrigeExamen(Long idEtudiant, Long examenId) throws EtudiantException;

}
