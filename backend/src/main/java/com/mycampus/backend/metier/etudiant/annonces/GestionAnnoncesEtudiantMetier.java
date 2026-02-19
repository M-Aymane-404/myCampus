package com.mycampus.backend.metier.etudiant.annonces;

import com.mycampus.backend.dto.annonces.AnnonceSortieEtudiantDTO;

import java.util.List;

public interface GestionAnnoncesEtudiantMetier {
    
    List<AnnonceSortieEtudiantDTO> listerAnnoncesVisibles(Long idEtudiant);
    
    AnnonceSortieEtudiantDTO trouverAnnonceParId(Long id, Long idEtudiant);
}
