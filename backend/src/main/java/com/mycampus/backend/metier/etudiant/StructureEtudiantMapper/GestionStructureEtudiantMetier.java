package com.mycampus.backend.metier.etudiant.StructureEtudiantMapper;

import com.mycampus.backend.dto.etudiant.DonneeSortie.EtudiantSortieDTO;

public interface GestionStructureEtudiantMetier {

    EtudiantSortieDTO trouverEtudiantConnecter(Long id);

}
