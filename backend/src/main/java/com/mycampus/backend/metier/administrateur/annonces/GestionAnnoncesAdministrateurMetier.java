package com.mycampus.backend.metier.administrateur.annonces;

import com.mycampus.backend.dto.annonces.AnnonceEntrerDTO;
import com.mycampus.backend.dto.annonces.AnnonceSortieDTO;

import java.util.List;

public interface GestionAnnoncesAdministrateurMetier {
    
    AnnonceSortieDTO creerAnnonce(AnnonceEntrerDTO annonceDTO, String emailAuteur);


    List<AnnonceSortieDTO> listerToutesAnnonces();
    
    List<AnnonceSortieDTO> listerAnnoncesParClasse(String codeClasse);
    
    void supprimerAnnonce(Long annonceId);
    
    AnnonceSortieDTO publierAnnonce(Long annonceId);
    
    AnnonceSortieDTO depublierAnnonce(Long annonceId);
}
