package com.mycampus.backend.metier.administrateur.utilisateurs;

import com.mycampus.backend.dto.AdministrateurDTO;
import com.mycampus.backend.entites.Administrateur;
import com.mycampus.backend.repositorys.AdministrateurRepository;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import com.mycampus.backend.mappers.AdministrateurMapper;
import com.mycampus.backend.exceptions.UtilisateursException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
;

@Service
@Transactional
@RequiredArgsConstructor
public class GestionUtilisateursMetierImpl implements GestionUtilisateursMetier {

    private final AdministrateurRepository administrateurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AdministrateurMapper administrateurMapper;

    @Override
    public AdministrateurDTO creerAdministrateur(AdministrateurDTO administrateurDTO) throws UtilisateursException {
        if (administrateurDTO == null) {
            throw new UtilisateursException("L'administrateur ne peut pas être nul");
        }
        
        if (administrateurDTO.getPpr() == null || administrateurDTO.getPpr().trim().isEmpty()) {
            throw new UtilisateursException("Le PPR de l'administrateur est obligatoire");
        }
        
        if (administrateurRepository.existsByPpr(administrateurDTO.getPpr())) {
            throw new UtilisateursException("Un administrateur avec ce PPR existe déjà");
        }
        
        if (administrateurDTO.getEmail() == null || administrateurDTO.getEmail().trim().isEmpty()) {
            throw new UtilisateursException("L'email de l'administrateur est obligatoire");
        }
        
        if (utilisateurRepository.existsByEmail(administrateurDTO.getEmail())) {
            throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
        }
        
        Administrateur administrateur = administrateurMapper.versAdministrateur(administrateurDTO);
        administrateur.setDateCreation(new Date());
        administrateur.setEnabled(true);
        
        Administrateur administrateurSauvegarde = administrateurRepository.save(administrateur);
        return administrateurMapper.versAdministrateurDTO(administrateurSauvegarde);
    }

    @Override
    public AdministrateurDTO trouverAdministrateurParId(Long id) throws UtilisateursException {
        if (id == null) {
            throw  new UtilisateursException(
                    "id est obligatoire"
            );
        }
        
        Administrateur administrateur = administrateurRepository.findById(id).orElseThrow(() ->
                new UtilisateursException("Administrateur non trouvé avec l'identifiant: " + id));
        return administrateurMapper.versAdministrateurDTO(administrateur);
    }

    @Override
    public AdministrateurDTO trouverAdministrateurParPpr(String ppr) throws UtilisateursException {
        if (ppr == null) {
            throw  new UtilisateursException(
                    "id est obligatoire"
            );
        }
        
        Administrateur administrateur = administrateurRepository.findByPpr(ppr).orElseThrow(() ->
                new UtilisateursException("Administrateur non trouvé avec l'identifiant: " + ppr));;
        return administrateurMapper.versAdministrateurDTO(administrateur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdministrateurDTO> listerTousLesAdministrateurs() throws UtilisateursException {
        try {
            List<Administrateur> administrateurs = administrateurRepository.findAll();

            
            return administrateurs.stream().map(administrateurMapper::versAdministrateurDTO).toList() ;
        } catch (Exception e) {
            throw new UtilisateursException("Erreur lors de la liste des administrateurs: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public AdministrateurDTO modifierAdministrateur(Long id, AdministrateurDTO administrateurDTO) throws UtilisateursException {
        if (id == null) {
            throw new UtilisateursException("L'identifiant de l'administrateur ne peut pas être nul");
        }
        
        Administrateur adminAModifier = administrateurRepository.findById(id).orElseThrow(()->
                new UtilisateursException("Administrateur non trouvé avec l'identifiant: " + id));

        

        if (administrateurDTO.getNom() != null && !administrateurDTO.getNom().trim().isEmpty()) {
            adminAModifier.setNom(administrateurDTO.getNom());
        }
        
        if (administrateurDTO.getPrenom() != null && !administrateurDTO.getPrenom().trim().isEmpty()) {
            adminAModifier.setPrenom(administrateurDTO.getPrenom());
        }
        
        if (administrateurDTO.getEmail() != null && !administrateurDTO.getEmail().trim().isEmpty()) {
            if (!adminAModifier.getEmail().equals(administrateurDTO.getEmail()) && 
                utilisateurRepository.existsByEmail(administrateurDTO.getEmail())) {
                throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
            }
            adminAModifier.setEmail(administrateurDTO.getEmail());
        }
        
        if (administrateurDTO.getFonction() != null) {
            adminAModifier.setFonction(administrateurDTO.getFonction());
        }
        
        if (administrateurDTO.getGrade() != null && !administrateurDTO.getGrade().trim().isEmpty()) {
            adminAModifier.setGrade(administrateurDTO.getGrade());
        }
        
        Administrateur administrateurModifie = administrateurRepository.save(adminAModifier);
        return administrateurMapper.versAdministrateurDTO(administrateurModifie);
    }

    @Override
    @Transactional
    public void supprimerAdministrateur(Long id) throws UtilisateursException {
        if (id == null) {
            throw new UtilisateursException("L'identifiant de l'administrateur ne peut pas être nul");
        }
        
        if (!administrateurRepository.existsById(id)) {
            throw new UtilisateursException("Administrateur non trouvé avec l'identifiant: " + id);
        }
        
        administrateurRepository.deleteById(id);
    }



    @Override
    @Transactional
    public void activerAdministrateur(Long id) throws UtilisateursException {
        Administrateur administrateur = administrateurRepository.findById(id).orElseThrow(()->
                new UtilisateursException("Administrateur non trouvé avec l'identifiant: " + id));

        
        administrateur.setEnabled(true);
        administrateurRepository.save(administrateur);
    }

    @Override
    @Transactional
    public void desactiverAdministrateur(Long id) throws UtilisateursException {
        Administrateur administrateur = administrateurRepository.findById(id).orElseThrow(()->
                new UtilisateursException("Administrateur non trouvé avec l'identifiant: " + id));



        administrateur.setEnabled(false);
        administrateurRepository.save(administrateur);
    }
}
