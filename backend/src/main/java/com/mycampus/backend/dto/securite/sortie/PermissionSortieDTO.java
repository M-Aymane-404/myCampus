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
public class PermissionSortieDTO {

    private Long id;

    private String codePermission;

    private String description;

    private LocalDateTime dateCreation;
}
