package com.mycampus.backend.metier.professeur.gestionUtilisateur;

import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.ProfesseurModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ProfesseurSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.ProfModificationProfDTO;
import com.mycampus.backend.exceptions.UtilisateursException;

public interface gestionProfesseurMetier {


    ProfesseurSortieDTO modifierProfesseur(Long id, ProfModificationProfDTO professeurModificationDTO) throws UtilisateursException;
}
