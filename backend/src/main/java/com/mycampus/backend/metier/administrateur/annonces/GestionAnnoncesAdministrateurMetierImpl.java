package com.mycampus.backend.metier.administrateur.annonces;

import com.mycampus.backend.dto.Notification.NotificationEntrerPourPlusieursUtilisateurDTO;
import com.mycampus.backend.dto.Notification.NotificationSortieDTO;
import com.mycampus.backend.dto.annonces.AnnonceEntrerDTO;
import com.mycampus.backend.dto.annonces.AnnonceSortieDTO;
import com.mycampus.backend.entites.Annonce;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Departement;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.enumerations.AnnonceCibleType;
import com.mycampus.backend.enumerations.ReferenceType;
import com.mycampus.backend.enumerations.TypeNotification;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.mappers.annonce.AnnonceMapper;
import com.mycampus.backend.metier.Notification.NotificationMetier;
import com.mycampus.backend.metier.Notification.NotificationWebSocketMetier;
import com.mycampus.backend.repositorys.AnnonceRepository;
import com.mycampus.backend.repositorys.ClasseRepository;
import com.mycampus.backend.repositorys.DepartementRepository;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GestionAnnoncesAdministrateurMetierImpl implements GestionAnnoncesAdministrateurMetier {

    private final AnnonceRepository annonceRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ClasseRepository classeRepository;
    private final AnnonceMapper  annonceMapper;
    private final DepartementRepository departementRepository;
    private final NotificationMetier notificationMetier;

    @Override
    @Transactional
    public AnnonceSortieDTO creerAnnonce(AnnonceEntrerDTO annonceDTO, Long idAuteur) {

        Utilisateur auteur = utilisateurRepository.findById(idAuteur)
                .orElseThrow(() -> new UtilisateursException("Auteur non trouvé"));
        


        Classe classe = null;
        Departement departement = null;

        switch (annonceDTO.getCibleType()) {
            case ETUDIANTS_CLASSE -> {
                if (annonceDTO.getCodeClasse() == null || annonceDTO.getCodeClasse().isBlank()) {
                    throw new UtilisateursException("Code classe obligatoire pour une annonce ETUDIANTS_CLASSE");
                }
                classe = classeRepository.findByCodeClasse(annonceDTO.getCodeClasse())
                        .orElseThrow(() -> new UtilisateursException("Classe non trouvée: " + annonceDTO.getCodeClasse()));
            }
            case PROFESSEURS_DEPARTEMENT -> {
                if (annonceDTO.getCodeDepartement() == null || annonceDTO.getCodeDepartement().isBlank()) {
                    throw new UtilisateursException("Code département obligatoire pour PROFESSEURS_DEPARTEMENT");
                }
                departement = departementRepository.findByCodeDepartement(annonceDTO.getCodeDepartement())
                        .orElseThrow(() -> new UtilisateursException("Département non trouvé: " + annonceDTO.getCodeDepartement()));
            }
            default -> {
             }
        }

        
        Annonce annonce = Annonce.builder()
                .titre(annonceDTO.getTitre())
                .contenu(annonceDTO.getContenu())
                .datePublication(LocalDateTime.now())
                 .publiee(true)
                .typeAnnonce(annonceDTO.getTypeAnnonce())
                .urgent(annonceDTO.isUrgent())
                .cibleType(annonceDTO.getCibleType())
                .auteur(auteur)
                .classe(classe)
                .departement(departement)
                .build();
        
        Annonce savedAnnonce = annonceRepository.save(annonce);


        NotificationEntrerPourPlusieursUtilisateurDTO dto = NotificationEntrerPourPlusieursUtilisateurDTO.builder()
                .type(TypeNotification.ANNONCE_PUBLIEE)
                .auteurId(auteur.getId())
                .titre(savedAnnonce.getTitre())
                .message("Nouvelle annonce: " + savedAnnonce.getTitre())
                .referenceType(ReferenceType.ANNONCE)
                .referenceId(savedAnnonce.getId())
                .build();



        switch (annonceDTO.getCibleType()) {
            case ETUDIANTS_CLASSE ->
                    notificationMetier.notifierEtudiantClasse(classe.getCodeClasse(), dto);

            case PROFESSEURS_DEPARTEMENT ->
                    notificationMetier.notifierProfesseurDepartement(departement.getCodeDepartement(), dto);

            case ETUDIANTS_GLOBAL ->
                    notificationMetier.notifierToutsEtudiant(dto);

            case PROFESSEURS_GLOBAL ->
                    notificationMetier.notifierToutsProfesseur(dto);
        }

        log.info("Annonce créée avec succès: {}", savedAnnonce.getId());
        
        return annonceMapper.changerVersSortieDTO(savedAnnonce);
    }





    @Override
    @Transactional(readOnly=true)
    public List<AnnonceSortieDTO> listerToutesAnnonces() {
        log.info("Listage de toutes les annonces");
        return annonceRepository.findAllWithAuteurAndClasse().stream()
                .map(annonceMapper::changerVersSortieDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly=true)
    public List<AnnonceSortieDTO> listerAnnoncesParClasse(String codeClasse) {
        log.info("Listage des annonces pour la classe: {}", codeClasse);
        return annonceRepository.findByClasse_CodeClasse(codeClasse).stream()
                .map(annonceMapper::changerVersSortieDTO)
                .toList();
    }

    @Override
    @Transactional
    public void supprimerAnnonce(Long annonceId) {
        log.info("Suppression de l'annonce: {}", annonceId);
        
        if (!annonceRepository.existsById(annonceId)) {
            throw new UtilisateursException("Annonce non trouvée");
        }
        
        annonceRepository.deleteById(annonceId);
        log.info("Annonce supprimée avec succès");
    }

    @Override
    @Transactional
    public AnnonceSortieDTO publierAnnonce(Long annonceId) {
        log.info("Publication de l'annonce: {}", annonceId);
        
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new UtilisateursException("Annonce non trouvée"));
        
        annonce.setPubliee(true);
        Annonce savedAnnonce = annonceRepository.save(annonce);
        log.info("Annonce publiée avec succès");
        
        return annonceMapper.changerVersSortieDTO(savedAnnonce);
    }

    @Override
    @Transactional
    public AnnonceSortieDTO depublierAnnonce(Long annonceId) {
        log.info("Dépublication de l'annonce: {}", annonceId);
        
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new UtilisateursException("Annonce non trouvée"));
        
        annonce.setPubliee(false);
        Annonce savedAnnonce = annonceRepository.save(annonce);
        log.info("Annonce dépubliée avec succès");
        
        return annonceMapper.changerVersSortieDTO(savedAnnonce);
    }

}
