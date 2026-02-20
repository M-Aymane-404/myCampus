package com.mycampus.backend.metier.professeur.presences;

import com.mycampus.backend.dto.professeur.DonneeEntrer.presences.PresenceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.LignePresenceSortieDTO;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.exceptions.UtilisateursException;

public interface GestionPresencesProfesseurMetier {
   /* List<PresenceSortieDTO> listerPresencesParSeance(String professeurPpr, Long seanceId)
            throws SeancesProfesseurException;*/

   LignePresenceSortieDTO saisirPresences(String professeurPpr, Long seanceId, PresenceEntrerDTO dto)
            throws SeancesProfesseurException;

    String TrouverProfesseurPprParemail(String email) throws UtilisateursException;

    // String TrouverProfesseurPprParemail(String email)throws UtilisateursException;
}


