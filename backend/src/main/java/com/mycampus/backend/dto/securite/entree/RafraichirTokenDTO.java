package com.mycampus.backend.dto.securite.entree;

import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RafraichirTokenDTO {

    @NotBlank(message = "refreshToken obligatoire")
    String refreshToken;
}
