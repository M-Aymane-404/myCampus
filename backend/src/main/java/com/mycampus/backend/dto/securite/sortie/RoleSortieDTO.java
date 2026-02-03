package com.mycampus.backend.dto.securite.sortie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleSortieDTO {

    private Long id;

    private String nomRole;

    private String description;

    private Boolean actif;

    private LocalDateTime dateCreation;

    private List<PermissionSortieDTO> permissions;
}
