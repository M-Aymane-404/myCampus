package com.mycampus.backend.dto.annonces;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClasseDTO {
    private Long id;
    private String codeClasse;
    private String nom;
    private String niveau;
}
