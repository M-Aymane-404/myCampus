package com.mycampus.backend.metier.professeur.seances;



import com.mycampus.backend.dto.administrateur.DonneSortie.AffectationEnseignementSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.fichiersSeance.DepotFichierSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.CreationSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.ModificationSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.RechercheSeancesEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.fichiersSeance.FichierSeanceSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.fichiersSeance.TelechargementFichierSeanceSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.seances.SeanceResumeSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.seances.SeanceSortieDTO;
import com.mycampus.backend.entites.AffectationEnseignement;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GestionSeancesProfesseurMetier {

    List<AffectationEnseignementSortieDTO> listerEnseignements(String professeurPpr)
            throws SeancesProfesseurException;

    SeanceSortieDTO creerSeance(String professeurPpr, CreationSeanceEntrerDTO creationSeanceEntrerDTO)
            throws SeancesProfesseurException;

    SeanceSortieDTO modifierSeance(String professeurPpr, Long seanceId, ModificationSeanceEntrerDTO modificationSeanceEntrerDTO)
            throws SeancesProfesseurException;

    void supprimerSeance(String professeurPpr, Long seanceId)
            throws SeancesProfesseurException;

    SeanceSortieDTO trouverSeanceParId(String professeurPpr, Long seanceId)
            throws SeancesProfesseurException;

    List<SeanceResumeSortieDTO> rechercherSeances(String professeurPpr, RechercheSeancesEntrerDTO rechercheSeancesEntrerDTO)
            throws SeancesProfesseurException;

    FichierSeanceSortieDTO deposerFichierPourSeance(String professeurPpr, DepotFichierSeanceEntrerDTO depotFichierSeanceEntrerDTO, MultipartFile fichier)
            throws SeancesProfesseurException;

    void supprimerFichierDeSeance(String professeurPpr, Long fichierSeanceId)
            throws SeancesProfesseurException;

    List<FichierSeanceSortieDTO> listerFichiersDeSeance(String professeurPpr, Long seanceId)
            throws SeancesProfesseurException;

    TelechargementFichierSeanceSortieDTO preparerTelechargementFichierSeance(String professeurPpr, Long fichierSeanceId)
            throws SeancesProfesseurException;

}