package com.mycampus.backend.metier.security;

import com.mycampus.backend.entites.Role;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

         utilisateur.getUtilisateurRoles().forEach(ur -> {

            Role role = ur.getRole();
            if(role.getActif()){
            authorities.add("ROLE_" + role.getNomRole());

             role.getRolePermissions().forEach(rp -> {
                authorities.add(rp.getPermission().getCodePermission());
            });
            }
        });

          Collection<GrantedAuthority> grantedAuthorities = authorities.stream()
                .distinct()
                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new).collect(Collectors.toSet());



        return CustomUserDetails.builder()
                .id(utilisateur.getId())
                .email(utilisateur.getEmail())
                .cin(utilisateur.getCin())
                .active(utilisateur.getActive())
                .password(utilisateur.getPassword())
                .authorities(grantedAuthorities)
                .build();





    }
}
