package com.mycampus.backend.dto.professeur.DonneeEntrer.presences;

import com.mycampus.backend.enumerations.StatusAbsence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class PresenceEntrerDTO {

    private Long seanceId;

    @NotBlank(message = "Apogée obligatoire")
    private String apogee;

    @NotNull(message = "Statut obligatoire")
    private StatusAbsence status;

    private boolean justifiee;

    private String motif;
}

