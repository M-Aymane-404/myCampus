package com.mycampus.backend.dto.securite.sortie;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JetonsSortieDTO {
    private String access_token;
    private String refresh_token;
}
