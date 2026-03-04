package com.mycampus.backend.metier.security;

import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GestionUtilisateurConnecteMetierImpl implements GestionUtilisateurConnecteMetier {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public Long trouverIdUtilisateurConnecte(String email) throws UtilisateursException {
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UtilisateursException("Utilisateur introuvable"));
        return u.getId();
    }
    @Override
    public String trouverProfesseurPprParId(Long id) throws UtilisateursException {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateursException("Utilisateur introuvable"));

        if (!(u instanceof Professeur prof)) {
            throw new UtilisateursException("Utilisateur connecté n'est pas un professeur.");
        }

        return prof.getPpr();
    }
}
