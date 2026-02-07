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
public class AttributionRoleUtilisateurSortieDTO {

    private Long id;

    private UtilisateurMiniDTO utilisateur;

    private RoleSortieDTO role;

    private LocalDateTime dateAttribution;

    private LocalDateTime expireLe;

    private Boolean actif;
}
