package com.mycampus.backend.metier.etudiant.annonces;

import com.mycampus.backend.dto.annonces.AnnonceSortieEtudiantDTO;
import com.mycampus.backend.entites.Annonce;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.enumerations.AnnonceCibleType;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.mappers.annonce.AnnonceEtudiantMapper;
import com.mycampus.backend.repositorys.AnnonceRepository;
import com.mycampus.backend.repositorys.ClasseRepository;
import com.mycampus.backend.repositorys.EtudiantRepository;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GestionAnnoncesEtudiantMetierImpl implements GestionAnnoncesEtudiantMetier {

    private final AnnonceRepository annonceRepository;
   // private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
     private final AnnonceEtudiantMapper annonceEtudiantMapper;

    @Override
    @Transactional(readOnly=true)
    public List<AnnonceSortieEtudiantDTO> listerAnnoncesVisibles(String emailEtudiant) {
        log.info("Listage des annonces visibles pour l'étudiant: {}", emailEtudiant);
        
        Etudiant etudiant =   etudiantRepository.findByEmail(emailEtudiant)
                .orElseThrow(() -> new UtilisateursException("Étudiant non trouvé"));
        
        if (etudiant.getClasse() == null) {
            log.info("L'étudiant n'a pas de classe, retour des annonces globales uniquement");
            return annonceRepository.findByCibleType(AnnonceCibleType.ETUDIANTS_GLOBAL).stream()
                    .filter(Annonce::isPubliee)
                    .map(annonceEtudiantMapper::changerVersSortieEtudiantDTO)
                    .collect(Collectors.toList());
        }
        
        List<Annonce> annoncesVisibles = annonceRepository.findByCibleTypeOrClasseId(
                AnnonceCibleType.ETUDIANTS_GLOBAL, etudiant.getClasse().getId());
        
        return annoncesVisibles.stream()
                .filter(Annonce::isPubliee)
                .map(annonceEtudiantMapper::changerVersSortieEtudiantDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly=true)
    public AnnonceSortieEtudiantDTO trouverAnnonceParId(Long id, String emailEtudiant) {
        log.info("Recherche de l'annonce {} pour l'étudiant: {}", id, emailEtudiant);
        
        Etudiant etudiant =  etudiantRepository.findByEmail(emailEtudiant)
                .orElseThrow(() -> new UtilisateursException("Étudiant non trouvé"));
        
        Annonce annonce = annonceRepository.findByAnnonce_Id(id)
                .orElseThrow(() -> new UtilisateursException("Annonce non trouvée"));
        
        if (!annonce.isPubliee()) {
            throw new UtilisateursException("Annonce non publiée");
        }
        
        if (annonce.getCibleType() == AnnonceCibleType.ETUDIANTS_GLOBAL) {
            return annonceEtudiantMapper.changerVersSortieEtudiantDTO(annonce);
        }
        
        if (annonce.getCibleType() == AnnonceCibleType.ETUDIANTS_CLASSE) {
            if (etudiant.getClasse() == null) {
                throw new UtilisateursException("Annonce de classe non accessible");
            }
            
            if (annonce.getClasse().getId().equals(etudiant.getClasse().getId())) {
                return annonceEtudiantMapper.changerVersSortieEtudiantDTO(annonce);
            }
        }
        
        throw new UtilisateursException("Annonce non accessible");
    }
}
