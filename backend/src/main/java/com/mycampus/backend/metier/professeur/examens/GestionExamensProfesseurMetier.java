package com.mycampus.backend.metier.professeur.examens;

import com.mycampus.backend.dto.professeur.DonneeEntrer.examens.ExamenCreationEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.examens.ExamenModificationEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.examens.ExamenRechercheEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.examens.ExamenRechercheSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.examens.ExamenSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.examens.FiltreExamensSortieDTO;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GestionExamensProfesseurMetier {

    FiltreExamensSortieDTO chargerFiltresExamens(String ppr);

    //List<ExamenRechercheSortieDTO> rechercherExamens(String ppr, ExamenRechercheEntrerDTO filtre);

    ExamenSortieDTO creerExamen(String ppr, String codeClasse, String codeMatiere, ExamenCreationEntrerDTO dto);

    ExamenSortieDTO modifierExamen(String ppr, Long examenId, ExamenModificationEntrerDTO dto);

    void supprimerExamen(String ppr, Long examenId);




    //ExamenSortieDTO deposerCorrige(String ppr, String codeClasse, String codeMatiere, Long examenId, MultipartFile pdf);

    //Resource telechargerCorrigeProf(String ppr, String codeClasse, String codeMatiere, Long examenId);

}
