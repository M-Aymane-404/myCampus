package com.mycampus.backend.metier.security;

import com.mycampus.backend.entites.Role;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.metier.etudiant.StructureEtudiantMapper.GestionStructureEtudiantMetierImpl;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class UserDetailsServiceImpl implements org.springframework.security.core.userdetails.UserDetailsService {
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

        List<String> authorities = new ArrayList<>();

        // 1️⃣ Ajouter les ROLES
        utilisateur.getUtilisateurRoles().forEach(ur -> {

            Role role = ur.getRole();
            if(role.getActif()){
            authorities.add("ROLE_" + role.getNomRole());

            // 2️⃣ Ajouter les PERMISSIONS du rôle
            role.getRolePermissions().forEach(rp -> {
                authorities.add(rp.getPermission().getCodePermission());
            });
            }
        });

        return User.withUsername(utilisateur.getEmail())
                .password(utilisateur.getPassword())
                .authorities(authorities.toArray(new String[0]))
                .accountLocked(!utilisateur.getActive())
                .credentialsExpired(false)
                .accountExpired(false)
                .disabled(!utilisateur.getActive())
                .build();

    }
}
