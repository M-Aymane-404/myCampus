package com.mycampus.backend.metier.professeur.annonces;

import com.mycampus.backend.dto.annonces.AnnonceEntrerDTO;
import com.mycampus.backend.dto.annonces.AnnonceSortieDTO;
import com.mycampus.backend.exceptions.UtilisateursException;

import java.util.List;

public interface GestionAnnoncesProfesseurMetier {
    
    AnnonceSortieDTO creerAnnonce(AnnonceEntrerDTO annonceDTO, String emailAuteur) throws UtilisateursException;
    
    List<AnnonceSortieDTO> listerAnnoncesDeSesClasses(String emailProfesseur) throws UtilisateursException;
    
    List<AnnonceSortieDTO> listerMesAnnonces(String emailProfesseur) throws UtilisateursException;

    List<AnnonceSortieDTO> listerLesAnnoncesDeMonDepEtGlobal(String emailProfesseur) throws  UtilisateursException;

    void supprimerMonAnnonce(Long annonceId, String emailProfesseur) throws UtilisateursException;
    
    AnnonceSortieDTO publierMonAnnonce(Long annonceId, String emailProfesseur) throws UtilisateursException;
    
    AnnonceSortieDTO depublierMonAnnonce(Long annonceId, String emailProfesseur) throws UtilisateursException;
}
