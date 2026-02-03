package com.mycampus.backend.dto.securite.entree;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributionRolePermissionDTO {

    @NotNull(message = "Identifiant du rôle obligatoire")
    private Long idRole;

    @NotNull(message = "Identifiant de la permission obligatoire")
    private Long idPermission;
}
