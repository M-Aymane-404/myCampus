package com.mycampus.backend.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@AllArgsConstructor
@SuperBuilder
public class AdministrateurDTO  extends UtilisateurDto{

    private String ppr;

    private String grade;

    private String fonction;
}
