package com.mycampus.backend.mappers.security;

import com.mycampus.backend.dto.securite.sortie.PermissionSortieDTO;
import com.mycampus.backend.dto.securite.sortie.RoleSortieDTO;
import com.mycampus.backend.entites.Permission;
import com.mycampus.backend.entites.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StructureSecuriteMapper {

    public RoleSortieDTO duRoleVersRoleSortieDTO(Role role) {
        if (role == null) {
            return null;
        }
        
        RoleSortieDTO roleSortieDTO = new RoleSortieDTO();
        BeanUtils.copyProperties(role, roleSortieDTO);
        
        // Convertir RolesCode en String
        if (role.getNomRole() != null) {
            roleSortieDTO.setNomRole(role.getNomRole().name());
        }
        
        // Mapper les permissions
        if (role.getRolePermissions() != null) {
            List<PermissionSortieDTO> permissions = role.getRolePermissions().stream()
                    .map(rp -> duPermissionVersPermissionSortieDTO(rp.getPermission()))
                    .collect(Collectors.toList());
            roleSortieDTO.setPermissions(permissions);
        }
        
        return roleSortieDTO;
    }

    public List<RoleSortieDTO> duListeRoleVersListRoleSortieDTO(List<Role> roles) {
        if (roles == null) {
            return List.of();
        }
        
        return roles.stream()
                .map(this::duRoleVersRoleSortieDTO)
                .collect(Collectors.toList());
    }

    public PermissionSortieDTO duPermissionVersPermissionSortieDTO(Permission permission) {
        if (permission == null) {
            return null;
        }
        
        PermissionSortieDTO permissionSortieDTO = new PermissionSortieDTO();
        BeanUtils.copyProperties(permission, permissionSortieDTO);
        
        return permissionSortieDTO;
    }

    public List<PermissionSortieDTO> duListePermissionVersListPermissionSortieDTO(List<Permission> permissions) {
        if (permissions == null) {
            return List.of();
        }
        
        return permissions.stream()
                .map(this::duPermissionVersPermissionSortieDTO)
                .collect(Collectors.toList());
    }
}
