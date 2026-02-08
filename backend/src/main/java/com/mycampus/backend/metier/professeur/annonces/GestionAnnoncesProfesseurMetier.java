package com.mycampus.backend.metier.professeur.annonces;

import com.mycampus.backend.dto.annonces.AnnonceEntrerDTO;
import com.mycampus.backend.dto.annonces.AnnonceSortieDTO;

import java.util.List;

public interface GestionAnnoncesProfesseurMetier {
    
    AnnonceSortieDTO creerAnnonce(AnnonceEntrerDTO annonceDTO, String emailAuteur);
    
    List<AnnonceSortieDTO> listerAnnoncesDeSesClasses(String emailProfesseur);
    
    List<AnnonceSortieDTO> listerMesAnnonces(String emailProfesseur);
    
    void supprimerMonAnnonce(Long annonceId, String emailProfesseur);
    
    AnnonceSortieDTO publierMonAnnonce(Long annonceId, String emailProfesseur);
    
    AnnonceSortieDTO depublierMonAnnonce(Long annonceId, String emailProfesseur);
}
