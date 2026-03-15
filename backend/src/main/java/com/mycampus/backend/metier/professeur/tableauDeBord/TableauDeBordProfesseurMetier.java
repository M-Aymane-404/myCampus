package com.mycampus.backend.metier.professeur.tableauDeBord;



import com.mycampus.backend.dto.professeur.DonneeSortie.tableauDeBord.TableauDeBordProfesseurSortieDTO;

public interface TableauDeBordProfesseurMetier {
    TableauDeBordProfesseurSortieDTO obtenirTableauDeBord(Long professeurId);
}
