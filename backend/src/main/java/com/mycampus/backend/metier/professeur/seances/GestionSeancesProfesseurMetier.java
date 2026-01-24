package com.mycampus.backend.metier.professeur.seances;



import com.mycampus.backend.dto.professeur.DonneeEntrer.fichiersSeance.DepotFichierSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.CreationSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.ModificationSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.RechercheSeancesEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.fichiersSeance.FichierSeanceSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.seances.SeanceResumeSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.seances.SeanceSortieDTO;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GestionSeancesProfesseurMetier {

    SeanceSortieDTO creerSeance(Long professeurId, CreationSeanceEntrerDTO creationSeanceEntrerDTO)
            throws SeancesProfesseurException;

    SeanceSortieDTO modifierSeance(Long professeurId, Long seanceId, ModificationSeanceEntrerDTO modificationSeanceEntrerDTO)
            throws SeancesProfesseurException;

    void supprimerSeance(Long professeurId, Long seanceId)
            throws SeancesProfesseurException;

    SeanceSortieDTO trouverSeanceParId(Long professeurId, Long seanceId)
            throws SeancesProfesseurException;

    List<SeanceResumeSortieDTO> rechercherSeances(Long professeurId, RechercheSeancesEntrerDTO rechercheSeancesEntrerDTO)
            throws SeancesProfesseurException;

    FichierSeanceSortieDTO deposerFichierPourSeance(Long professeurId, DepotFichierSeanceEntrerDTO depotFichierSeanceEntrerDTO, MultipartFile fichier)
            throws SeancesProfesseurException;

    void supprimerFichierDeSeance(Long professeurId, Long fichierSeanceId)
            throws SeancesProfesseurException;

    List<FichierSeanceSortieDTO> listerFichiersDeSeance(Long professeurId, Long seanceId)
            throws SeancesProfesseurException;
}
