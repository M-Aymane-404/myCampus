package com.mycampus.backend.dto.administrateur.DonneSortie;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProfesseurMiniSortieDTO  {
    private String ppr;

    private String nom;

    private String prenom;






}
