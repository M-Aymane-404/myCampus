package com.mycampus.backend.metier.Notification;

import com.mycampus.backend.dto.Notification.NotificationEntrerPourPlusieursUtilisateurDTO;
import com.mycampus.backend.dto.Notification.NotificationEntrerDTO;
import com.mycampus.backend.dto.Notification.NotificationSortieDTO;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Notification;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.exceptions.NotificationException;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.mappers.Notification.NotificationMappers;
import com.mycampus.backend.repositorys.EtudiantRepository;
import com.mycampus.backend.repositorys.NotificationRepository;
import com.mycampus.backend.repositorys.ProfesseurRepository;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationMetierImpl implements NotificationMetier {
    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketMetier notificationWebSocketMetier;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationMappers notificationMappers;
    private  final EtudiantRepository  etudiantRepository;
    private final ProfesseurRepository professeurRepository;


    @Override
    public NotificationSortieDTO notifierUtilisateur(NotificationEntrerDTO notificationEntrerDTO) throws NotificationException {


         if (notificationEntrerDTO == null) {
             throw new NotificationException("Notification ne peut pas être null");
         }
         if (notificationEntrerDTO.getTitre() == null || notificationEntrerDTO.getTitre().isBlank()) {
             throw new NotificationException("Titre obligatoire");
         }
         if (notificationEntrerDTO.getMessage() == null || notificationEntrerDTO.getMessage().isBlank()) {
             throw new NotificationException("Message obligatoire");
         }
         if (notificationEntrerDTO.getType() == null) {
             throw new NotificationException("Type obligatoire");
         }
         if (notificationEntrerDTO.getReferenceId() == null || notificationEntrerDTO.getReferenceType() == null) {
             throw new NotificationException("ReferenceId et ReferenceType obligatoires");
         }


         Utilisateur utilisateurQuiRecoit = utilisateurRepository.findById(notificationEntrerDTO.getUtilisateurRecoitId())
                 .orElseThrow(()->new UtilisateursException("Utilisateur qui vas reçoit notification non existant"));

         Utilisateur auteur = requireAuteur(notificationEntrerDTO.getAuteurId());
        Notification notification = Notification.builder()
                .utilisateur( utilisateurQuiRecoit)
                .titre(notificationEntrerDTO.getTitre())
                .message(notificationEntrerDTO.getMessage())
                .type(notificationEntrerDTO.getType())
                .dateEnvoi(LocalDateTime.now())
                .auteur(auteur)
                .referenceId(notificationEntrerDTO.getReferenceId())
                .referenceType(notificationEntrerDTO.getReferenceType())

                .lu(false)
                .build();

        notificationRepository.save(notification);

NotificationSortieDTO notificationSortieDTO =  notificationMappers.notificationToNotificationSortieDTO(notification);
notificationSortieDTO.setNomCompletAuteur(auteur.getNom() + " " + auteur.getPrenom());
notificationSortieDTO.setNomCompletUtilisateurRecoit(utilisateurQuiRecoit.getNom() + " " + utilisateurQuiRecoit.getPrenom());

        notificationWebSocketMetier.notifierUtilisateur(
                utilisateurQuiRecoit.getEmail(),
               notificationSortieDTO
        );

         return notificationSortieDTO;

     }
    @Override
    public void notifierEtudiantClasse(String codeClasse, NotificationEntrerPourPlusieursUtilisateurDTO notificationEntrerPourPlusieursUtilisateurDTO) throws NotificationException {

         List<Etudiant> utilisateursQuiRecoit =  etudiantRepository.findAllByClasse_CodeClasse(codeClasse);
         Utilisateur auteur = requireAuteur(notificationEntrerPourPlusieursUtilisateurDTO.getAuteurId());


         NotificationEntrerDTO notificationEntrerDTO = new NotificationEntrerDTO();
         BeanUtils.copyProperties(notificationEntrerPourPlusieursUtilisateurDTO, notificationEntrerDTO);
         notificationEntrerDTO.setAuteurId(auteur.getId());
         for(Etudiant u : utilisateursQuiRecoit){

             notificationEntrerDTO.setUtilisateurRecoitId(u.getId());
             notifierUtilisateur(notificationEntrerDTO);
         }

    }
    @Override
    public void notifierProfesseurDepartement(String codeDepartement, NotificationEntrerPourPlusieursUtilisateurDTO notificationEntrerPourPlusieursUtilisateurDTO) throws NotificationException {

         List<Professeur> utilisateursQuiRecoit =  professeurRepository.findAllByDepartementOuTravail_CodeDepartement(codeDepartement);
         Utilisateur auteur =  requireAuteur(notificationEntrerPourPlusieursUtilisateurDTO.getAuteurId());
         NotificationEntrerDTO notificationEntrerDTO = new NotificationEntrerDTO();
         BeanUtils.copyProperties(notificationEntrerPourPlusieursUtilisateurDTO, notificationEntrerDTO);
         for(Professeur u : utilisateursQuiRecoit){

             notificationEntrerDTO.setUtilisateurRecoitId(u.getId());
             notifierUtilisateur(notificationEntrerDTO);
         }

     }


     private Utilisateur requireAuteur(Long id) throws UtilisateursException {
      return utilisateurRepository.findById(id)
                 .orElseThrow(()-> new UtilisateursException("auteur introuvable"));


     }
}
