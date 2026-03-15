package com.mycampus.backend.metier.administrateur.tableauDeBord;

import com.mycampus.backend.dto.administrateur.DonneSortie.TableauBordDTO.TableauBordAdministratifDTO;

public interface StatistiqueMetier {
    TableauBordAdministratifDTO obtenirTableauDeBordAdministratif();
}
