package com.mycampus.backend.metier.security;

import com.mycampus.backend.dto.securite.entree.AttributionRolePermissionDTO;
import com.mycampus.backend.dto.securite.entree.PermissionCreationDTO;
import com.mycampus.backend.dto.securite.entree.PermissionModificationDTO;
import com.mycampus.backend.dto.securite.entree.RoleCreationDTO;
import com.mycampus.backend.dto.securite.entree.RoleModificationDTO;
import com.mycampus.backend.dto.securite.sortie.PermissionSortieDTO;
import com.mycampus.backend.dto.securite.sortie.RoleSortieDTO;

import java.util.List;

public interface GestionDesRolesEtPermissionsMetier {

    // GESTION DES RÔLES
    RoleSortieDTO creerRole(RoleCreationDTO dto);
    
    RoleSortieDTO modifierRole(String codeRole, RoleModificationDTO dto);
    
    void supprimerRole(String codeRole);
    
    List<RoleSortieDTO> listerRoles();
    
    RoleSortieDTO trouverRoleParCode(String codeRole);

    // GESTION DES PERMISSIONS
    PermissionSortieDTO creerPermission(PermissionCreationDTO dto);
    
    PermissionSortieDTO modifierPermission(String codePermission, PermissionModificationDTO dto);
    
    void supprimerPermission(String codePermission);
    
    List<PermissionSortieDTO> listerPermissions();
    
    PermissionSortieDTO trouverPermissionParCode(String codePermission);

    // GESTION RÔLE ↔ PERMISSION
    void ajouterPermissionAuRole(String codeRole, String codePermission);
    
    void retirerPermissionDuRole(String codeRole, String codePermission);
    
    List<PermissionSortieDTO> listerPermissionsDUnRole(String codeRole);

    // GESTION UTILISATEUR ↔ RÔLE
    void attribuerRoleAUtilisateur(Long idUtilisateur, String codeRole);
    
    void retirerRoleAUtilisateur(Long idUtilisateur, String codeRole);
    
    List<RoleSortieDTO> listerRolesDUnUtilisateur(Long idUtilisateur);
}
