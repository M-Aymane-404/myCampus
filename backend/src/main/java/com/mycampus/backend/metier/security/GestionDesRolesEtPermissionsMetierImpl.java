package com.mycampus.backend.metier.security;

import com.mycampus.backend.dto.securite.entree.AttributionRolePermissionDTO;
import com.mycampus.backend.dto.securite.entree.PermissionCreationDTO;
import com.mycampus.backend.dto.securite.entree.PermissionModificationDTO;
import com.mycampus.backend.dto.securite.entree.RoleCreationDTO;
import com.mycampus.backend.dto.securite.entree.RoleModificationDTO;
import com.mycampus.backend.dto.securite.sortie.PermissionSortieDTO;
import com.mycampus.backend.dto.securite.sortie.RoleSortieDTO;
import com.mycampus.backend.enumerations.RolesCode;
import com.mycampus.backend.exceptions.SecuriteException;
import com.mycampus.backend.mappers.security.StructureSecuriteMapper;
import com.mycampus.backend.entites.*;
import com.mycampus.backend.repositorys.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GestionDesRolesEtPermissionsMetierImpl implements GestionDesRolesEtPermissionsMetier {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final StructureSecuriteMapper mapper;

    // GESTION DES RÔLES
    @Override
    public RoleSortieDTO creerRole(RoleCreationDTO dto) {
        log.info("Création du rôle: {}", dto.getNomRole());
        

            RolesCode roleCode = RolesCode.valueOf(dto.getNomRole());
            
            if (roleRepository.existsByNomRole(roleCode)) {
                throw new SecuriteException("Le rôle " + dto.getNomRole() + " existe déjà");
            }
            
            Role role = Role.builder()
                    .nomRole(roleCode)
                    .description(dto.getDescription())
                    .actif(dto.getActif() != null ? dto.getActif() : true)
                    .dateCreation(LocalDateTime.now())
                    .build();
            
            Role savedRole = roleRepository.save(role);
            log.info("Rôle créé avec succès: {}", savedRole.getNomRole());
            
            return mapper.duRoleVersRoleSortieDTO(savedRole);

    }

    @Override
    public RoleSortieDTO modifierRole(String codeRole, RoleModificationDTO dto) {
        log.info("Modification du rôle: {}", codeRole);
        
        RolesCode roleCode = RolesCode.valueOf(codeRole);
        Role role = requireRole(roleCode);
        
        if (dto.getDescription() != null) {
            role.setDescription(dto.getDescription());
        }
        
        if (dto.getActif() != null) {
            role.setActif(dto.getActif());
        }
        
        Role savedRole = roleRepository.save(role);
        log.info("Rôle modifié avec succès: {}", savedRole.getNomRole());
        
        return mapper.duRoleVersRoleSortieDTO(savedRole);
    }

    @Override
    public void supprimerRole(String codeRole) {
        log.info("Suppression du rôle: {}", codeRole);
        
        RolesCode roleCode = RolesCode.valueOf(codeRole);
        Role role = requireRole(roleCode);
        
        if (!role.getUtilisateurRoles().isEmpty()) {
            throw new SecuriteException("Impossible de supprimer le rôle " + codeRole + " car il est attribué à des utilisateurs");
        }
        
        roleRepository.delete(role);
        log.info("Rôle supprimé avec succès: {}", codeRole);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleSortieDTO> listerRoles() {
        List<Role> roles = roleRepository.findAll();
        return mapper.duListeRoleVersListRoleSortieDTO(roles);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleSortieDTO trouverRoleParCode(String codeRole) {
        RolesCode roleCode = RolesCode.valueOf(codeRole);
        Role role = requireRole(roleCode);
        return mapper.duRoleVersRoleSortieDTO(role);
    }

    // GESTION DES PERMISSIONS
    @Override
    public PermissionSortieDTO creerPermission(PermissionCreationDTO dto) {
        log.info("Création de la permission: {}", dto.getCodePermission());
        
        if (permissionRepository.existsByCodePermission(dto.getCodePermission())) {
            throw new SecuriteException("La permission " + dto.getCodePermission() + " existe déjà");
        }
        
        Permission permission = Permission.builder()
                .codePermission(dto.getCodePermission())
                .description(dto.getDescription())
                .dateCreation(LocalDateTime.now())
                .build();
        
        Permission savedPermission = permissionRepository.save(permission);
        log.info("Permission créée avec succès: {}", savedPermission.getCodePermission());
        
        return mapper.duPermissionVersPermissionSortieDTO(savedPermission);
    }

    @Override
    public PermissionSortieDTO modifierPermission(String codePermission, PermissionModificationDTO dto) {
        log.info("Modification de la permission: {}", codePermission);
        
        Permission permission = requirePermission(codePermission);
        
        if (dto.getDescription() != null) {
            permission.setDescription(dto.getDescription());
        }
        
        Permission savedPermission = permissionRepository.save(permission);
        log.info("Permission modifiée avec succès: {}", savedPermission.getCodePermission());
        
        return mapper.duPermissionVersPermissionSortieDTO(savedPermission);
    }

    @Override
    public void supprimerPermission(String codePermission) {
        log.info("Suppression de la permission: {}", codePermission);
        
        Permission permission = requirePermission(codePermission);
        
        if (!permission.getRolePermissions().isEmpty()) {
            throw new SecuriteException("Impossible de supprimer la permission " + codePermission + " car elle est attribuée à des rôles");
        }
        
        permissionRepository.delete(permission);
        log.info("Permission supprimée avec succès: {}", codePermission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionSortieDTO> listerPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return mapper.duListePermissionVersListPermissionSortieDTO(permissions);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionSortieDTO trouverPermissionParCode(String codePermission) {
        Permission permission = requirePermission(codePermission);
        return mapper.duPermissionVersPermissionSortieDTO(permission);
    }

    // GESTION RÔLE ↔ PERMISSION
    @Override
    public void ajouterPermissionAuRole(String codeRole, String codePermission) {
        log.info("Ajout de la permission {} au rôle {}", codePermission, codeRole);
        
        RolesCode roleCode = RolesCode.valueOf(codeRole);
        Role role = requireRole(roleCode);
        Permission permission = requirePermission(codePermission);
        
        boolean exists = role.getRolePermissions().stream()
                .anyMatch(rp -> rp.getPermission().getCodePermission().equals(codePermission));
        
        if (exists) {
            log.info("La permission {} est déjà attribuée au rôle {}", codePermission, codeRole);
            return;
        }
        
        RolePermission rolePermission = RolePermission.builder()
                .role(role)
                .permission(permission)
                .dateAttribution(LocalDateTime.now())
                .build();
        
        role.getRolePermissions().add(rolePermission);
        roleRepository.save(role);
        
        log.info("Permission {} attribuée au rôle {} avec succès", codePermission, codeRole);
    }

    @Override
    public void retirerPermissionDuRole(String codeRole, String codePermission) {
        log.info("Retrait de la permission {} du rôle {}", codePermission, codeRole);
        
        RolesCode roleCode = RolesCode.valueOf(codeRole);
        Role role = requireRole(roleCode);
        
        RolePermission rolePermission = role.getRolePermissions().stream()
                .filter(rp -> rp.getPermission().getCodePermission().equals(codePermission))
                .findFirst()
                .orElseThrow(() -> new SecuriteException("La permission " + codePermission + " n'est pas attribuée au rôle " + codeRole));
        
        role.getRolePermissions().remove(rolePermission);
        roleRepository.save(role);
        
        log.info("Permission {} retirée du rôle {} avec succès", codePermission, codeRole);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionSortieDTO> listerPermissionsDUnRole(String codeRole) {
        RolesCode roleCode = RolesCode.valueOf(codeRole);
        Role role = requireRole(roleCode);
        
        return role.getRolePermissions().stream()
                .map(rp -> mapper.duPermissionVersPermissionSortieDTO(rp.getPermission()))
                .toList();
    }

    // GESTION UTILISATEUR ↔ RÔLE
    @Override
    public void attribuerRoleAUtilisateur(Long idUtilisateur, String codeRole) {
        log.info("Attribution du rôle {} à l'utilisateur {}", codeRole, idUtilisateur);
        
        Utilisateur utilisateur = requireUtilisateur(idUtilisateur);
        RolesCode roleCode = RolesCode.valueOf(codeRole);
        Role role = requireRole(roleCode);
        
        boolean exists = utilisateur.getUtilisateurRoles().stream()
                .anyMatch(ur -> ur.getRole().getNomRole().equals(roleCode));
        
        if (exists) {
            log.info("L'utilisateur {} a déjà le rôle {}", idUtilisateur, codeRole);
            return;
        }
        
        UtilisateurRole utilisateurRole = UtilisateurRole.builder()
                .utilisateur(utilisateur)
                .role(role)
                .dateAttribution(LocalDateTime.now())
                .actif(true)
                .build();
        
        utilisateur.getUtilisateurRoles().add(utilisateurRole);
        utilisateurRepository.save(utilisateur);
        
        log.info("Rôle {} attribué à l'utilisateur {} avec succès", codeRole, idUtilisateur);
    }

    @Override
    public void retirerRoleAUtilisateur(Long idUtilisateur, String codeRole) {
        log.info("Retrait du rôle {} de l'utilisateur {}", codeRole, idUtilisateur);
        
        Utilisateur utilisateur = requireUtilisateur(idUtilisateur);
        RolesCode roleCode = RolesCode.valueOf(codeRole);
        
        UtilisateurRole utilisateurRole = utilisateur.getUtilisateurRoles().stream()
                .filter(ur -> ur.getRole().getNomRole().equals(roleCode))
                .findFirst()
                .orElseThrow(() -> new SecuriteException("L'utilisateur " + idUtilisateur + " n'a pas le rôle " + codeRole));
        
        utilisateur.getUtilisateurRoles().remove(utilisateurRole);
        utilisateurRepository.save(utilisateur);
        
        log.info("Rôle {} retiré de l'utilisateur {} avec succès", codeRole, idUtilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleSortieDTO> listerRolesDUnUtilisateur(Long idUtilisateur) {
        Utilisateur utilisateur = requireUtilisateur(idUtilisateur);
        
        return utilisateur.getUtilisateurRoles().stream()
                .map(ur -> mapper.duRoleVersRoleSortieDTO(ur.getRole()))
                .toList();
    }

    // MÉTHODES PRIVÉES
    private Role requireRole(RolesCode roleCode) {
        return roleRepository.findByNomRole(roleCode)
                .orElseThrow(() -> new SecuriteException("Rôle introuvable: " + roleCode));
    }

    private Permission requirePermission(String codePermission) {
        return permissionRepository.findByCodePermission(codePermission)
                .orElseThrow(() -> new SecuriteException("Permission introuvable: " + codePermission));
    }

    private Utilisateur requireUtilisateur(Long idUtilisateur) {
        return utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new SecuriteException("Utilisateur introuvable: " + idUtilisateur));
    }
}
