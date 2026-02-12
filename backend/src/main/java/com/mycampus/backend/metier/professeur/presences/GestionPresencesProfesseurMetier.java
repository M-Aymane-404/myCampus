package com.mycampus.backend.metier.professeur.presences;

import com.mycampus.backend.dto.professeur.DonneeEntrer.presences.TableauPresencesEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.PresenceSortieDTO;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.exceptions.UtilisateursException;

import java.util.List;

public interface GestionPresencesProfesseurMetier {
    List<PresenceSortieDTO> listerPresencesParSeance(String professeurPpr, Long seanceId)
            throws SeancesProfesseurException;

    List<PresenceSortieDTO> saisirPresences(String professeurPpr, Long seanceId, TableauPresencesEntrerDTO dto)
            throws SeancesProfesseurException;

    String TrouverProfesseurpprParemail(String email)throws UtilisateursException;
}


