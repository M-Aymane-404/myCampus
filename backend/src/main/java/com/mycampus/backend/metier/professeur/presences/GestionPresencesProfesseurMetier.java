package com.mycampus.backend.metier.professeur.presences;

import com.mycampus.backend.dto.professeur.DonneeEntrer.presences.PresenceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.EtudiantPresenceSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.FeuilleAppelPresenceSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.presences.LignePresenceSortieDTO;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.exceptions.UtilisateursException;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface GestionPresencesProfesseurMetier {
   /* List<PresenceSortieDTO> listerPresencesParSeance(String professeurPpr, Long seanceId)
            throws SeancesProfesseurException;*/

    @Transactional(readOnly = true)
    List<EtudiantPresenceSortieDTO> listerEtudiantsparCodeClasse(String professeurPpr, String codeClasse)
            throws UtilisateursException, SeancesProfesseurException;

    /* public List<EtudiantPresenceSortieDTO> listerEtudiantsparCodeClasse(String professeurPpr, String codeClasse )
              throws UtilisateursException, SeancesProfesseurException {
              Professeur prof= requireProfesseurPpr(professeurPpr);
               if (codeClasse==null){
                   throw new UtilisateursException("code Classe obligatoire.");
               }

          }*/
    @Transactional(readOnly = true)
    FeuilleAppelPresenceSortieDTO chargerFeuilleAppelPresence(String professeurPpr, Long seanceId)
            throws SeancesProfesseurException;

    LignePresenceSortieDTO saisirPresences(String professeurPpr, Long seanceId, PresenceEntrerDTO dto)
            throws SeancesProfesseurException;

    LignePresenceSortieDTO validerJustificatif(String professeurPpr, Long seanceId, Long presenceId)
            throws SeancesProfesseurException;

    LignePresenceSortieDTO refuserJustificatif(String professeurPpr, Long seanceId, Long presenceId)
            throws SeancesProfesseurException;

    Resource telechargerJustificatif(String professeurPpr, Long seanceId, Long presenceId)
            throws SeancesProfesseurException;

   // String TrouverProfesseurPprParemail(String email) throws UtilisateursException;

    // String TrouverProfesseurPprParemail(String email)throws UtilisateursException;
}


