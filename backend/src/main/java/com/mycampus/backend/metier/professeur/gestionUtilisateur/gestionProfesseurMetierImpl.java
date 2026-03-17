package com.mycampus.backend.metier.professeur.gestionUtilisateur;

import com.mycampus.backend.dto.administrateur.DonneSortie.ProfesseurSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.ProfModificationProfDTO;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.mappers.administrateur.UtilisateursMapper;
import com.mycampus.backend.repositorys.ProfesseurRepository;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class gestionProfesseurMetierImpl implements  gestionProfesseurMetier {

      private final ProfesseurRepository  professeurRepository;
      private final UtilisateurRepository utilisateurRepository;
      private final PasswordEncoder passwordEncoder;
      private final UtilisateursMapper utilisateursMapper;


      @Override
      public ProfesseurSortieDTO modifierProfesseur(Long id, ProfModificationProfDTO professeurModificationDTO) throws UtilisateursException {
        if (id == null  ) {
            throw new UtilisateursException("Le id du professeur ne peut pas être nul");
        }

        if (professeurModificationDTO == null) {
            throw new UtilisateursException("Le professeur ne peut pas être nul");
        }

        Professeur professeurAModifier = professeurRepository.findById(id).orElseThrow(() ->
                new UtilisateursException("Professeur introuvable avec le id: " + id));








        if(professeurModificationDTO.getTel() != null && !professeurModificationDTO.getTel().trim().isEmpty()) {
            if(!professeurAModifier.getTel().equals(professeurModificationDTO.getTel()) && utilisateurRepository.existsByTel(professeurModificationDTO.getTel())){
                throw new UtilisateursException("Un utilisateur avec cet numero existe déjà");

            }
            professeurAModifier.setTel(professeurModificationDTO.getTel());
        }











          if (professeurModificationDTO.getNouveauPassword() != null && !professeurModificationDTO.getNouveauPassword().trim().isEmpty()) {

              if (professeurModificationDTO.getAncienPassword() == null || professeurModificationDTO.getAncienPassword().trim().isEmpty()) {
                  throw new UtilisateursException("Ancien mot de passe obligatoire");
              }

              boolean ok = passwordEncoder.matches(professeurModificationDTO.getAncienPassword(), professeurAModifier.getPassword());

              if (!ok) {
                  throw new UtilisateursException("Mot de passe incorrect");
              }

              professeurAModifier.setPassword(passwordEncoder.encode(professeurModificationDTO.getNouveauPassword()));
          }

        Professeur professeurModifie = professeurRepository.save(professeurAModifier);
        return utilisateursMapper.FromProfesseurVersProfesseurSortirDTO(professeurModifie);
    }

}
