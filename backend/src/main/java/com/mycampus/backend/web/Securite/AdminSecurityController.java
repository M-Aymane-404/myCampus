package com.mycampus.backend.web.Securite;

import com.mycampus.backend.dto.securite.entree.PermissionCreationDTO;
import com.mycampus.backend.dto.securite.entree.PermissionModificationDTO;
import com.mycampus.backend.dto.securite.entree.RoleCreationDTO;
import com.mycampus.backend.dto.securite.entree.RoleModificationDTO;
import com.mycampus.backend.dto.securite.sortie.PermissionSortieDTO;
import com.mycampus.backend.dto.securite.sortie.RoleSortieDTO;
import com.mycampus.backend.metier.security.GestionDesRolesEtPermissionsMetier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/securite")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN_SECURITE_GERER')")
public class AdminSecurityController {

    private final GestionDesRolesEtPermissionsMetier metier;

    // ROLE CRUD
    @PostMapping("/roles")
    @Operation(summary = "Créer un nouveau rôle")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rôle créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Rôle déjà existant")
    })
    @PreAuthorize("hasAuthority('SECURITE_ROLE_CREER')")
    public ResponseEntity<RoleSortieDTO> creerRole(@Valid @RequestBody RoleCreationDTO dto) {
        RoleSortieDTO result = metier.creerRole(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/roles")
    @Operation(summary = "Lister tous les rôles")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des rôles récupérée")
    })
    @PreAuthorize("hasAuthority('SECURITE_ROLE_LIRE')")
    public ResponseEntity<List<RoleSortieDTO>> listerRoles() {
        return ResponseEntity.ok(metier.listerRoles());
    }

    @GetMapping("/roles/{codeRole}")
    @Operation(summary = "Trouver un rôle par son code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rôle trouvé"),
            @ApiResponse(responseCode = "404", description = "Rôle introuvable")
    })
    @PreAuthorize("hasAuthority('SECURITE_ROLE_LIRE')")
    public ResponseEntity<RoleSortieDTO> trouverRole(@PathVariable String codeRole) {
        return ResponseEntity.ok(metier.trouverRoleParCode(codeRole));
    }

    @PutMapping("/roles/{codeRole}")
    @Operation(summary = "Modifier un rôle")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rôle modifié"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Rôle introuvable")
    })
    @PreAuthorize("hasAuthority('SECURITE_ROLE_MODIFIER')")
    public ResponseEntity<RoleSortieDTO> modifierRole(
            @PathVariable String codeRole,
            @Valid @RequestBody RoleModificationDTO dto) {
        return ResponseEntity.ok(metier.modifierRole(codeRole, dto));
    }

    @DeleteMapping("/roles/{codeRole}")
    @Operation(summary = "Supprimer un rôle")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rôle supprimé"),
            @ApiResponse(responseCode = "404", description = "Rôle introuvable"),
            @ApiResponse(responseCode = "400", description = "Rôle utilisé par des utilisateurs")
    })
    @PreAuthorize("hasAuthority('SECURITE_ROLE_SUPPRIMER')")
    public ResponseEntity<Void> supprimerRole(@PathVariable String codeRole) {
        metier.supprimerRole(codeRole);
        return ResponseEntity.noContent().build();
    }

    // PERMISSION CRUD
    @PostMapping("/permissions")
    @Operation(summary = "Créer une nouvelle permission")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Permission créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Permission déjà existante")
    })
    @PreAuthorize("hasAuthority('SECURITE_PERMISSION_CREER')")
    public ResponseEntity<PermissionSortieDTO> creerPermission(@Valid @RequestBody PermissionCreationDTO dto) {
        PermissionSortieDTO result = metier.creerPermission(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/permissions")
    @Operation(summary = "Lister toutes les permissions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des permissions récupérée")
    })
    @PreAuthorize("hasAuthority('SECURITE_PERMISSION_LIRE')")
    public ResponseEntity<List<PermissionSortieDTO>> listerPermissions() {
        return ResponseEntity.ok(metier.listerPermissions());
    }

    @GetMapping("/permissions/{codePermission}")
    @Operation(summary = "Trouver une permission par son code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permission trouvée"),
            @ApiResponse(responseCode = "404", description = "Permission introuvable")
    })
    @PreAuthorize("hasAuthority('SECURITE_PERMISSION_LIRE')")
    public ResponseEntity<PermissionSortieDTO> trouverPermission(@PathVariable String codePermission) {
        return ResponseEntity.ok(metier.trouverPermissionParCode(codePermission));
    }

    @PutMapping("/permissions/{codePermission}")
    @Operation(summary = "Modifier une permission")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permission modifiée"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Permission introuvable")
    })
    @PreAuthorize("hasAuthority('SECURITE_PERMISSION_MODIFIER')")
    public ResponseEntity<PermissionSortieDTO> modifierPermission(
            @PathVariable String codePermission,
            @Valid @RequestBody PermissionModificationDTO dto) {
        return ResponseEntity.ok(metier.modifierPermission(codePermission, dto));
    }

    @DeleteMapping("/permissions/{codePermission}")
    @Operation(summary = "Supprimer une permission")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Permission supprimée"),
            @ApiResponse(responseCode = "404", description = "Permission introuvable"),
            @ApiResponse(responseCode = "400", description = "Permission utilisée par des rôles")
    })
    @PreAuthorize("hasAuthority('SECURITE_PERMISSION_SUPPRIMER')")
    public ResponseEntity<Void> supprimerPermission(@PathVariable String codePermission) {
        metier.supprimerPermission(codePermission);
        return ResponseEntity.noContent().build();
    }

    // ROLE-PERMISSION MANAGEMENT
    @PostMapping("/roles/{codeRole}/permissions/{codePermission}")
    @Operation(summary = "Attribuer une permission à un rôle")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Permission attribuée au rôle"),
            @ApiResponse(responseCode = "404", description = "Rôle ou permission introuvable"),
            @ApiResponse(responseCode = "409", description = "Permission déjà attribuée au rôle")
    })
    @PreAuthorize("hasAuthority('SECURITE_ROLE_PERMISSION_ATTRIBUER')")
    public ResponseEntity<Void> attribuerPermissionAuRole(
            @PathVariable String codeRole,
            @PathVariable String codePermission) {
        metier.ajouterPermissionAuRole(codeRole, codePermission);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/roles/{codeRole}/permissions/{codePermission}")
    @Operation(summary = "Retirer une permission d'un rôle")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Permission retirée du rôle"),
            @ApiResponse(responseCode = "404", description = "Rôle ou permission introuvable"),
            @ApiResponse(responseCode = "404", description = "Permission non attribuée au rôle")
    })
    @PreAuthorize("hasAuthority('SECURITE_ROLE_PERMISSION_RETIRER')")
    public ResponseEntity<Void> retirerPermissionDuRole(
            @PathVariable String codeRole,
            @PathVariable String codePermission) {
        metier.retirerPermissionDuRole(codeRole, codePermission);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles/{codeRole}/permissions")
    @Operation(summary = "Lister les permissions d'un rôle")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des permissions du rôle récupérée"),
            @ApiResponse(responseCode = "404", description = "Rôle introuvable")
    })
    @PreAuthorize("hasAuthority('SECURITE_ROLE_PERMISSION_LIRE')")
    public ResponseEntity<List<PermissionSortieDTO>> listerPermissionsRole(@PathVariable String codeRole) {
        return ResponseEntity.ok(metier.listerPermissionsDUnRole(codeRole));
    }

    // USER-ROLE MANAGEMENT
    @PostMapping("/utilisateurs/{idUtilisateur}/roles/{codeRole}")
    @Operation(summary = "Attribuer un rôle à un utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rôle attribué à l'utilisateur"),
            @ApiResponse(responseCode = "404", description = "Utilisateur ou rôle introuvable"),
            @ApiResponse(responseCode = "409", description = "Rôle déjà attribué à l'utilisateur")
    })
    @PreAuthorize("hasAuthority('SECURITE_UTILISATEUR_ROLE_ATTRIBUER')")
    public ResponseEntity<Void> attribuerRoleAUtilisateur(
            @PathVariable Long idUtilisateur,
            @PathVariable String codeRole) {
        metier.attribuerRoleAUtilisateur(idUtilisateur, codeRole);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/utilisateurs/{idUtilisateur}/roles/{codeRole}")
    @Operation(summary = "Retirer un rôle d'un utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rôle retiré de l'utilisateur"),
            @ApiResponse(responseCode = "404", description = "Utilisateur ou rôle introuvable"),
            @ApiResponse(responseCode = "404", description = "Rôle non attribué à l'utilisateur")
    })
    @PreAuthorize("hasAuthority('SECURITE_UTILISATEUR_ROLE_RETIRER')")
    public ResponseEntity<Void> retirerRoleAUtilisateur(
            @PathVariable Long idUtilisateur,
            @PathVariable String codeRole) {
        metier.retirerRoleAUtilisateur(idUtilisateur, codeRole);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/utilisateurs/{idUtilisateur}/roles")
    @Operation(summary = "Lister les rôles d'un utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des rôles de l'utilisateur récupérée"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @PreAuthorize("hasAuthority('SECURITE_UTILISATEUR_ROLE_LIRE')")
    public ResponseEntity<List<RoleSortieDTO>> listerRolesUtilisateur(@PathVariable Long idUtilisateur) {
        return ResponseEntity.ok(metier.listerRolesDUnUtilisateur(idUtilisateur));
    }
}
