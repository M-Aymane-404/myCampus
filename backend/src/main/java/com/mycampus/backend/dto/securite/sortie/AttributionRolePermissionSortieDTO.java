package com.mycampus.backend.dto.securite.sortie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributionRolePermissionSortieDTO {

    private Long id;

    private RoleSortieDTO role;

    private PermissionSortieDTO permission;

    private LocalDateTime dateAttribution;
}
