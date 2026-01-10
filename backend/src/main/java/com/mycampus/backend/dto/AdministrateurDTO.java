package com.mycampus.backend.dto;

import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data

public class AdministrateurDTO  extends UtilisateurDto{

    private String ppr;

    private String grade;

    private String fonction;
}
