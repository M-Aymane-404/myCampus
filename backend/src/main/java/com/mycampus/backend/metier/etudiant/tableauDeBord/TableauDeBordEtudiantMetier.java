package com.mycampus.backend.metier.etudiant.tableauDeBord;

import com.mycampus.backend.dto.etudiant.tableauDeBord.TableauDeBordEtudiantSortieDTO;

public interface TableauDeBordEtudiantMetier {
    
    TableauDeBordEtudiantSortieDTO obtenirTableauDeBord(String emailEtudiant);
}
