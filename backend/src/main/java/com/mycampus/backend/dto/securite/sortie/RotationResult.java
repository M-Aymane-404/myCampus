package com.mycampus.backend.dto.securite.sortie;

 import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RotationResult {
    String email;
    String refreshToken;
}
