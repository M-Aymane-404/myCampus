package com.mycampus.backend.dto.securite.entree;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleModificationDTO {

    @Size(max = 200, message = "Description ne doit pas dépasser 200 caractères")
    private String description;

    private Boolean actif;
}
