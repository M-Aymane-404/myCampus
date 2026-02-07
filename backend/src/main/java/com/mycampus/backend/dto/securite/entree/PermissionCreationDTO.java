package com.mycampus.backend.dto.securite.entree;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionCreationDTO {

    @NotBlank(message = "Code permission obligatoire")
    @Pattern(
            regexp = "^[A-Z_]+$",
            message = "Code permission doit contenir uniquement des majuscules et underscores"
    )
    @Size(max = 50, message = "Code permission ne doit pas dépasser 50 caractères")
    private String codePermission;



    @Size(max = 200, message = "Description ne doit pas dépasser 200 caractères")
    private String description;


}
