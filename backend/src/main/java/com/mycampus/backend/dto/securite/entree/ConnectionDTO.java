package com.mycampus.backend.dto.securite.entree;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectionDTO {

    @NotBlank(message = "email obligatoire")
    @Size(max = 50, message = "email ne doit pas dépasser 50 caractères")

    private String email;

    @NotBlank(message = "moteDePasse obligatoire")
    @Size(max = 50, message = "  mote De Passe  ne doit pas dépasser 50 caractères")

    private String moteDePasse;
}
