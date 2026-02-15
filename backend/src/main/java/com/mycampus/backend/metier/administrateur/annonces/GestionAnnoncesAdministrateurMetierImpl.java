package com.mycampus.backend.metier.administrateur.annonces;

import com.mycampus.backend.dto.Notification.NotificationSortieDTO;
import com.mycampus.backend.dto.annonces.AnnonceEntrerDTO;
import com.mycampus.backend.dto.annonces.AnnonceSortieDTO;
import com.mycampus.backend.entites.Annonce;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Departement;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.enumerations.AnnonceCibleType;
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
    public AnnonceSortieDTO creerAnnonce(AnnonceEntrerDTO annonceDTO, String emailAuteur) {
        log.info("Création d'annonce par administrateur: {}", emailAuteur);
        
        Utilisateur auteur = utilisateurRepository.findByEmail(emailAuteur)
                .orElseThrow(() -> new UtilisateursException("Auteur non trouvé"));
        
        Classe classe = null;
        Departement departement = null;

        if (annonceDTO.getCibleType() == AnnonceCibleType.ETUDIANTS_CLASSE) {
            if (annonceDTO.getCodeClasse() == null) {
                throw new UtilisateursException("Code classe obligatoire pour une annonce de type CLASSE");
            }
            classe = classeRepository.findByCodeClasse(annonceDTO.getCodeClasse())
                    .orElseThrow(() -> new UtilisateursException("Classe non trouvée: " + annonceDTO.getCodeClasse()));

          


        }

        if (annonceDTO.getCibleType() == AnnonceCibleType.PROFESSEURS_DEPARTEMENT) {
            if (annonceDTO.getCodeDepartement() == null) {
                throw new UtilisateursException("Code Département obligatoire pour une annonce de type PROFESSEURS_DEPARTEMENT");
            }
            departement = departementRepository.findByCodeDepartement(annonceDTO.getCodeDepartement())
                    .orElseThrow(() -> new UtilisateursException("Département non trouvée: " + annonceDTO.getCodeDepartement()));
        }

        
        Annonce annonce = Annonce.builder()
                .titre(annonceDTO.getTitre())
                .contenu(annonceDTO.getContenu())
                .datePublication(LocalDateTime.now())
                 .publiee(true)
                .cibleType(annonceDTO.getCibleType())
                .auteur(auteur)
                .classe(classe)
                .departement(departement)
                .build();
        
        Annonce savedAnnonce = annonceRepository.save(annonce);
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
