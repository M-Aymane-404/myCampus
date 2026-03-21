package com.mycampus.backend.dto.annonces;

import com.mycampus.backend.enumerations.AnnonceCibleType;
import com.mycampus.backend.enumerations.TypeAnnonce;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnnonceEntrerDTO {
    
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200, message = "Le titre ne doit pas dépasser 200 caractères")
    private String titre;
    
    @NotBlank(message = "Le contenu est obligatoire")
    @Size(max = 2000, message = "Le contenu ne doit pas dépasser 2000 caractères")
    private String contenu;
    
    private TypeAnnonce typeAnnonce;

    @NotNull(message = "Le type de cible est obligatoire")
    private AnnonceCibleType cibleType;
    
    private String codeClasse;

    private String codeDepartement;

    @JsonAlias({"professeursIds", "professeurId"})
    private List<Long> professeurIds;

    @JsonAlias({"professeursPprs", "professeurPpr"})
    private List<String> professeurPprs;


}
