package com.mycampus.backend.metier.professeur.annonces;

import com.mycampus.backend.dto.Notification.NotificationEntrerPourPlusieursUtilisateurDTO;
import com.mycampus.backend.dto.annonces.AnnonceEntrerDTO;
import com.mycampus.backend.dto.annonces.AnnonceSortieDTO;
import com.mycampus.backend.entites.Annonce;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Departement;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.enumerations.AnnonceCibleType;
import com.mycampus.backend.enumerations.ReferenceType;
import com.mycampus.backend.enumerations.TypeNotification;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.mappers.annonce.AnnonceMapper;
import com.mycampus.backend.metier.Notification.NotificationMetier;
import com.mycampus.backend.metier.annonces.ressources.GestionRessourcesAnnonceMetier;
import com.mycampus.backend.repositorys.AnnonceRepository;
import com.mycampus.backend.repositorys.ClasseRepository;
import com.mycampus.backend.repositorys.NotificationRepository;
import com.mycampus.backend.repositorys.ProfesseurRepository;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GestionAnnoncesProfesseurMetierImpl implements GestionAnnoncesProfesseurMetier {

    private final AnnonceRepository annonceRepository;
    private final ClasseRepository classeRepository;
   private final UtilisateurRepository utilisateurRepository;
    private final ProfesseurRepository professeurRepository;
     private final AnnonceMapper  annonceMapper;
    private final NotificationMetier notificationMetier;
    private final NotificationRepository notificationRepository;
    private final GestionRessourcesAnnonceMetier gestionRessourcesAnnonceMetier;

    @Override
    @Transactional
    public AnnonceSortieDTO creerAnnonce(AnnonceEntrerDTO annonceDTO, String emailAuteur) throws UtilisateursException {
        log.info("Création d'annonce par professeur: {}", emailAuteur);
        
        Professeur   auteur = professeurRepository.findByEmail(emailAuteur)
                .orElseThrow(() -> new UtilisateursException("Auteur non trouvé"));


        if (annonceDTO.getCibleType() == AnnonceCibleType.ETUDIANTS_GLOBAL) {
            throw new UtilisateursException("Un professeur ne peut pas créer d'annonce globale");
        }
        
        if (annonceDTO.getCibleType() == AnnonceCibleType.ETUDIANTS_CLASSE) {
            if (annonceDTO.getCodeClasse() == null) {
                throw new UtilisateursException("Code classe obligatoire pour une annonce de type CLASSE");
            }
            
            Classe classe = classeRepository.findByCodeClasse(annonceDTO.getCodeClasse())
                    .orElseThrow(() -> new UtilisateursException("Classe non trouvée: " + annonceDTO.getCodeClasse()));
            
            List<Classe> classesProfesseur = classeRepository.findClassesByProfesseur(auteur);
            if (!classesProfesseur.contains(classe)) {
                throw new UtilisateursException("Le professeur n'est pas assigné à cette classe");
            }
            
            Annonce annonce = Annonce.builder()
                    .titre(annonceDTO.getTitre())
                    .contenu(annonceDTO.getContenu())
                    .datePublication(LocalDateTime.now())
                     .publiee(true)
                    .typeAnnonce(annonceDTO.getTypeAnnonce())
                    .cibleType(annonceDTO.getCibleType())
                    .auteur(auteur)
                    .classe(classe)
                    .build();
            
            Annonce savedAnnonce = annonceRepository.save(annonce);
            notifierAnnoncePubliee(savedAnnonce, auteur.getId());
            log.info("Annonce créée avec succès: {}", savedAnnonce.getId());
            
            return annonceMapper.changerVersSortieDTO(savedAnnonce);
        }
        
        throw new UtilisateursException("Type de cible non valide");
    }

    @Override
    @Transactional(readOnly=true)
    public List<AnnonceSortieDTO> listerAnnoncesDeSesClasses(String emailProfesseur)  throws UtilisateursException {
        log.info("Listage des annonces des classes du professeur: {}", emailProfesseur);
        
        Professeur professeur =  professeurRepository.findByEmail(emailProfesseur)
                .orElseThrow(() -> new UtilisateursException("Professeur non trouvé"));
        
        List<Classe> classesProfesseur = classeRepository.findClassesByProfesseur(professeur);
        if (classesProfesseur.isEmpty()) {
            // retourner uniquement les globales
            return annonceRepository.findByCibleType(AnnonceCibleType.ETUDIANTS_GLOBAL).stream()
                    .map(annonceMapper::changerVersSortieDTO)
                    .collect(Collectors.toList());
        }
        List<Annonce> annonces = annonceRepository.findGlobalAndClassAnnoncesDeProf(classesProfesseur);
        
        return annonces.stream()
                .map(annonceMapper::changerVersSortieDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly=true)
    public List<AnnonceSortieDTO> listerMesAnnonces(String emailProfesseur) throws  UtilisateursException {
        log.info("Listage des annonces du professeur: {}", emailProfesseur);
        
        Professeur professeur =  professeurRepository.findByEmail(emailProfesseur)
                .orElseThrow(() -> new UtilisateursException("Professeur non trouvé"));
        
        return annonceRepository.findByAuteur_Id(professeur.getId()).stream()
                .map(annonceMapper::changerVersSortieDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AnnonceSortieDTO recupererAnnonceParId(Long annonceId, String emailProfesseur) throws UtilisateursException {
        log.info("Récupération de l'annonce {} pour le professeur {}", annonceId, emailProfesseur);

        Professeur professeur = professeurRepository.findByEmail(emailProfesseur)
                .orElseThrow(() -> new UtilisateursException("Professeur non trouvé"));

        Annonce annonce = annonceRepository.findByAnnonce_Id(annonceId)
                .orElseThrow(() -> new UtilisateursException("Annonce non trouvée"));

        if (annonce.getAuteur() != null && professeur.getId().equals(annonce.getAuteur().getId())) {
            return annonceMapper.changerVersSortieDTO(annonce);
        }

        if (annonce.getCibleType() == AnnonceCibleType.PROFESSEURS_GLOBAL) {
            return annonceMapper.changerVersSortieDTO(annonce);
        }

        if (annonce.getCibleType() == AnnonceCibleType.PROFESSEURS_DEPARTEMENT
                && estAnnonceDepartementVisiblePourProfesseur(annonce, professeur)) {
            return annonceMapper.changerVersSortieDTO(annonce);
        }

        if (annonce.getCibleType() == AnnonceCibleType.ETUDIANTS_GLOBAL
                || annonce.getCibleType() == AnnonceCibleType.ETUDIANTS_CLASSE) {
            return annonceMapper.changerVersSortieDTO(annonce);
        }

        throw new UtilisateursException("Vous n'êtes pas autorisé à consulter cette annonce");
    }


    @Override
    @Transactional(readOnly=true)
    public List<AnnonceSortieDTO> listerLesAnnoncesDeMonDepEtGlobal(String emailProfesseur) throws  UtilisateursException {
        log.info("Listage des annonces du professeur: {}", emailProfesseur);

        Professeur professeur =  professeurRepository.findByEmail(emailProfesseur)
                .orElseThrow(() -> new UtilisateursException("Professeur non trouvé"));

        return annonceRepository.findAnnoncesProfesseursGlobalEtCiblees().stream()
                .filter(annonce -> estAnnonceVisiblePourProfesseur(annonce, professeur))
                .map(annonceMapper::changerVersSortieDTO)
                .toList();
    }

    @Override
     public void supprimerMonAnnonce(Long annonceId, String emailProfesseur) throws UtilisateursException {
        log.info("Suppression de l'annonce: {} par le professeur: {}", annonceId, emailProfesseur);
        
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new UtilisateursException("Annonce non trouvée"));
        
        Professeur professeur =  professeurRepository.findByEmail(emailProfesseur)
                .orElseThrow(() -> new UtilisateursException("Professeur non trouvé"));
        
        if (!annonce.getAuteur().getId().equals(professeur.getId())) {
            throw new UtilisateursException("Vous ne pouvez supprimer que vos propres annonces");
        }
        
        gestionRessourcesAnnonceMetier.supprimerRessourcesSilencieusement(annonce);
        annonceRepository.delete(annonce);
        log.info("Annonce supprimée avec succès");
    }

    @Override
     public AnnonceSortieDTO publierMonAnnonce(Long annonceId, String emailProfesseur) throws  UtilisateursException {
        log.info("Publication de l'annonce: {} par le professeur: {}", annonceId, emailProfesseur);
        
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new UtilisateursException("Annonce non trouvée"));
        
        Professeur professeur =  professeurRepository.findByEmail(emailProfesseur)
                .orElseThrow(() -> new UtilisateursException("Professeur non trouvé"));
        
        if (!annonce.getAuteur().getId().equals(professeur.getId())) {
            throw new UtilisateursException("Vous ne pouvez publier que vos propres annonces");
        }
        
        annonce.setPubliee(true);
        Annonce savedAnnonce = annonceRepository.save(annonce);
        notifierAnnoncePubliee(savedAnnonce, professeur.getId());
        log.info("Annonce publiée avec succès");
        
        return annonceMapper.changerVersSortieDTO(savedAnnonce);
    }

    @Override
     public AnnonceSortieDTO depublierMonAnnonce(Long annonceId, String emailProfesseur) throws UtilisateursException {
        log.info("Dépublication de l'annonce: {} par le professeur: {}", annonceId, emailProfesseur);
        
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new UtilisateursException("Annonce non trouvée"));
        
        Professeur professeur =  professeurRepository.findByEmail(emailProfesseur)
                .orElseThrow(() -> new UtilisateursException("Professeur non trouvé"));
        
        if (!annonce.getAuteur().getId().equals(professeur.getId())) {
            throw new UtilisateursException("Vous ne pouvez dépublier que vos propres annonces");
        }
        
        annonce.setPubliee(false);
        Annonce savedAnnonce = annonceRepository.save(annonce);
        log.info("Annonce dépubliée avec succès");
        
        return annonceMapper.changerVersSortieDTO(savedAnnonce);
    }

    private void notifierAnnoncePubliee(Annonce annonce, Long auteurId) {
        NotificationEntrerPourPlusieursUtilisateurDTO dto =
                NotificationEntrerPourPlusieursUtilisateurDTO.builder()
                        .type(TypeNotification.ANNONCE_PUBLIEE)
                        .auteurId(auteurId)
                        .titre(annonce.getTitre())
                        .message("Nouvelle annonce: " + annonce.getTitre())
                        .referenceType(ReferenceType.ANNONCE)
                        .referenceId(annonce.getId())
                        .build();

        if (annonce.getCibleType() == AnnonceCibleType.ETUDIANTS_CLASSE
                && annonce.getClasse() != null) {
            notificationMetier.notifierEtudiantClasse(
                    annonce.getClasse().getCodeClasse(),
                    dto
            );
        }
    }

    private boolean estAnnonceVisiblePourProfesseur(Annonce annonce, Professeur professeur) {
        if (annonce.getCibleType() != AnnonceCibleType.PROFESSEURS_DEPARTEMENT) {
            return true;
        }
        return estAnnonceDepartementVisiblePourProfesseur(annonce, professeur);
    }

    private boolean estAnnonceDepartementVisiblePourProfesseur(Annonce annonce, Professeur professeur) {
        if (annonce.getAuteur() != null && professeur.getId().equals(annonce.getAuteur().getId())) {
            return true;
        }

        boolean annonceAUnCiblageNotification = notificationRepository.existsByTypeAndReferenceTypeAndReferenceId(
                TypeNotification.ANNONCE_PUBLIEE,
                ReferenceType.ANNONCE,
                annonce.getId()
        );

        if (!annonceAUnCiblageNotification) {
            Departement depAnnonce = annonce.getDepartement();
            Departement depProf = professeur.getDepartementOuTravail();
            return depAnnonce != null
                    && depProf != null
                    && depAnnonce.getCodeDepartement().equals(depProf.getCodeDepartement());
        }

        return notificationRepository.existsByUtilisateur_IdAndTypeAndReferenceTypeAndReferenceId(
                professeur.getId(),
                TypeNotification.ANNONCE_PUBLIEE,
                ReferenceType.ANNONCE,
                annonce.getId()
        );
    }

}
