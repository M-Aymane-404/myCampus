package com.mycampus.backend.dto.annonces;

import com.mycampus.backend.enumerations.NiveauAnnee;
import jakarta.validation.constraints.NotBlank;
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

     private NiveauAnnee niveau;}
