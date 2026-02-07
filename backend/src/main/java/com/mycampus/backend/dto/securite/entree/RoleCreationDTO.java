package com.mycampus.backend.dto.securite.entree;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleCreationDTO {

    @NotBlank(message = "Nom du rôle obligatoire")
    @Size(max = 50, message = "Nom du rôle ne doit pas dépasser 50 caractères")
    private String nomRole;

    @Size(max = 200, message = "Description ne doit pas dépasser 200 caractères")
    private String description;

    private Boolean actif;
}
