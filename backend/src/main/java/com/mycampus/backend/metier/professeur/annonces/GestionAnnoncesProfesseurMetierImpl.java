package com.mycampus.backend.metier.professeur.annonces;

import com.mycampus.backend.dto.annonces.AnnonceEntrerDTO;
import com.mycampus.backend.dto.annonces.AnnonceSortieDTO;
import com.mycampus.backend.entites.Annonce;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.enumerations.AnnonceCibleType;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.mappers.annonce.AnnonceMapper;
import com.mycampus.backend.repositorys.AnnonceRepository;
import com.mycampus.backend.repositorys.ClasseRepository;
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
@Slf4j
public class GestionAnnoncesProfesseurMetierImpl implements GestionAnnoncesProfesseurMetier {

    private final AnnonceRepository annonceRepository;
    private final ClasseRepository classeRepository;
    private final UtilisateurRepository utilisateurRepository;
     private final AnnonceMapper  annonceMapper;

    @Override
    @Transactional
    public AnnonceSortieDTO creerAnnonce(AnnonceEntrerDTO annonceDTO, String emailAuteur) {
        log.info("Création d'annonce par professeur: {}", emailAuteur);
        
        Utilisateur auteur = utilisateurRepository.findByEmail(emailAuteur)
                .orElseThrow(() -> new UtilisateursException("Auteur non trouvé"));
        
        if (!(auteur instanceof Professeur)) {
            throw new UtilisateursException("Seul un professeur peut créer une annonce");
        }

        Professeur professeur = (Professeur) auteur;

        if (annonceDTO.getCibleType() == AnnonceCibleType.GLOBAL) {
            throw new UtilisateursException("Un professeur ne peut pas créer d'annonce globale");
        }
        
        if (annonceDTO.getCibleType() == AnnonceCibleType.CLASSE) {
            if (annonceDTO.getCodeClasse() == null) {
                throw new UtilisateursException("Code classe obligatoire pour une annonce de type CLASSE");
            }
            
            Classe classe = classeRepository.findByCodeClasse(annonceDTO.getCodeClasse())
                    .orElseThrow(() -> new UtilisateursException("Classe non trouvée: " + annonceDTO.getCodeClasse()));
            
            List<Classe> classesProfesseur = classeRepository.findClassesByProfesseur(professeur);
            if (!classesProfesseur.contains(classe)) {
                throw new UtilisateursException("Le professeur n'est pas assigné à cette classe");
            }
            
            Annonce annonce = Annonce.builder()
                    .titre(annonceDTO.getTitre())
                    .contenu(annonceDTO.getContenu())
                    .datePublication(LocalDateTime.now())
                     .publiee(false)
                    .cibleType(annonceDTO.getCibleType())
                    .auteur(auteur)
                    .classe(classe)
                    .build();
            
            Annonce savedAnnonce = annonceRepository.save(annonce);
            log.info("Annonce créée avec succès: {}", savedAnnonce.getId());
            
            return annonceMapper.changerVersSortieDTO(savedAnnonce);
        }
        
        throw new UtilisateursException("Type de cible non valide");
    }

    @Override
    public List<AnnonceSortieDTO> listerAnnoncesDeSesClasses(String emailProfesseur) {
        log.info("Listage des annonces des classes du professeur: {}", emailProfesseur);
        
        Professeur professeur = (Professeur) utilisateurRepository.findByEmail(emailProfesseur)
                .orElseThrow(() -> new UtilisateursException("Professeur non trouvé"));
        
        List<Classe> classesProfesseur = classeRepository.findClassesByProfesseur(professeur);
        List<Annonce> annonces = annonceRepository.findGlobalAndClassAnnonces(classesProfesseur);
        
        return annonces.stream()
                .map(annonceMapper::changerVersSortieDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnonceSortieDTO> listerMesAnnonces(String emailProfesseur) {
        log.info("Listage des annonces du professeur: {}", emailProfesseur);
        
        Professeur professeur = (Professeur) utilisateurRepository.findByEmail(emailProfesseur)
                .orElseThrow(() -> new UtilisateursException("Professeur non trouvé"));
        
        return annonceRepository.findByAuteur_Id(professeur.getId()).stream()
                .map(annonceMapper::changerVersSortieDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void supprimerMonAnnonce(Long annonceId, String emailProfesseur) {
        log.info("Suppression de l'annonce: {} par le professeur: {}", annonceId, emailProfesseur);
        
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new UtilisateursException("Annonce non trouvée"));
        
        Professeur professeur = (Professeur) utilisateurRepository.findByEmail(emailProfesseur)
                .orElseThrow(() -> new UtilisateursException("Professeur non trouvé"));
        
        if (!annonce.getAuteur().getId().equals(professeur.getId())) {
            throw new UtilisateursException("Vous ne pouvez supprimer que vos propres annonces");
        }
        
        annonceRepository.deleteById(annonceId);
        log.info("Annonce supprimée avec succès");
    }

    @Override
    @Transactional
    public AnnonceSortieDTO publierMonAnnonce(Long annonceId, String emailProfesseur) {
        log.info("Publication de l'annonce: {} par le professeur: {}", annonceId, emailProfesseur);
        
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new UtilisateursException("Annonce non trouvée"));
        
        Professeur professeur = (Professeur) utilisateurRepository.findByEmail(emailProfesseur)
                .orElseThrow(() -> new UtilisateursException("Professeur non trouvé"));
        
        if (!annonce.getAuteur().getId().equals(professeur.getId())) {
            throw new UtilisateursException("Vous ne pouvez publier que vos propres annonces");
        }
        
        annonce.setPubliee(true);
        Annonce savedAnnonce = annonceRepository.save(annonce);
        log.info("Annonce publiée avec succès");
        
        return annonceMapper.changerVersSortieDTO(savedAnnonce);
    }

    @Override
    @Transactional
    public AnnonceSortieDTO depublierMonAnnonce(Long annonceId, String emailProfesseur) {
        log.info("Dépublication de l'annonce: {} par le professeur: {}", annonceId, emailProfesseur);
        
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new UtilisateursException("Annonce non trouvée"));
        
        Professeur professeur = (Professeur) utilisateurRepository.findByEmail(emailProfesseur)
                .orElseThrow(() -> new UtilisateursException("Professeur non trouvé"));
        
        if (!annonce.getAuteur().getId().equals(professeur.getId())) {
            throw new UtilisateursException("Vous ne pouvez dépublier que vos propres annonces");
        }
        
        annonce.setPubliee(false);
        Annonce savedAnnonce = annonceRepository.save(annonce);
        log.info("Annonce dépubliée avec succès");
        
        return annonceMapper.changerVersSortieDTO(savedAnnonce);
    }


}
