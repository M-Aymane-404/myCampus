package com.mycampus.backend.dto.securite.entree;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributionRoleUtilisateurDTO {

    @NotNull(message = "Identifiant de l'utilisateur obligatoire")
    private Long idUtilisateur;

    @NotNull(message = "Identifiant du rôle obligatoire")
    private Long idRole;

    private LocalDateTime dateAttribution;
}
