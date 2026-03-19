package com.mycampus.backend.metier.security;

import com.mycampus.backend.entites.Presence;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.exceptions.PresenceException;
import com.mycampus.backend.exceptions.SecuriteException;
import com.mycampus.backend.metier.professeur.StockageFichiersService;
import com.mycampus.backend.repositorys.PresenceRepository;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccesFichiersSecurisesMetier {

    private final UtilisateurRepository utilisateurRepository;
    private final PresenceRepository presenceRepository;
    private final StockageFichiersService stockageFichiersService;

    public Resource chargerPhotoProfil(CustomUserDetails utilisateurConnecte, String photoNom) {
        if (!StringUtils.hasText(photoNom)) {
            throw new SecuriteException("Photo introuvable.");
        }

        Utilisateur proprietaire = utilisateurRepository.findByPhotoNom(photoNom)
                .orElseThrow(() -> new SecuriteException("Photo introuvable."));

        boolean administrateur = aAutorite(utilisateurConnecte, "ADMIN_UTILISATEUR_GERER");
        boolean proprietairePhoto = utilisateurConnecte != null && proprietaire.getId().equals(utilisateurConnecte.getId());
        if (!administrateur && !proprietairePhoto) {
            throw new SecuriteException("Acces refuse a cette photo.");
        }

        return stockageFichiersService.chargerDepuisSousDossier("usersPhotos", photoNom);
    }

    public Resource chargerJustificatifPresence(CustomUserDetails utilisateurConnecte, Long presenceId) {
        Presence presence = presenceRepository.findAvecRelationsPourAccesFichier(presenceId)
                .orElseThrow(() -> new PresenceException("Presence introuvable."));

        if (!StringUtils.hasText(presence.getMotifNom())) {
            throw new PresenceException("Aucun justificatif disponible.");
        }

        boolean proprietaire = utilisateurConnecte != null && presence.getEtudiant() != null
                && presence.getEtudiant().getId().equals(utilisateurConnecte.getId());
        boolean professeurLie = utilisateurConnecte != null
                && presence.getSeance() != null
                && presence.getSeance().getAffectationEnseignement() != null
                && presence.getSeance().getAffectationEnseignement().getProfesseur() != null
                && presence.getSeance().getAffectationEnseignement().getProfesseur().getId().equals(utilisateurConnecte.getId());
        boolean administrateur = aAutorite(utilisateurConnecte, "ADMIN_UTILISATEUR_GERER");

        if (!proprietaire && !professeurLie && !administrateur) {
            throw new SecuriteException("Acces refuse a ce justificatif.");
        }

        return stockageFichiersService.chargerDepuisSousDossier("motifsPresence", presence.getMotifNom());
    }

    private boolean aAutorite(CustomUserDetails utilisateurConnecte, String autorite) {
        return utilisateurConnecte != null
                && utilisateurConnecte.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(autorite::equals);
    }
}
