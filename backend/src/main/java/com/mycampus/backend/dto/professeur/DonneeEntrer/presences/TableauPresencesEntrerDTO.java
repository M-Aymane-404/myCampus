package com.mycampus.backend.dto.professeur.DonneeEntrer.presences;

import com.mycampus.backend.entites.Presence;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class TableauPresencesEntrerDTO {
    @NotNull(message = "SeanceId obligatoire")
    private Long seanceId;

    @NotEmpty(message = "La liste des présences est obligatoire")
    @Valid
    private List<PresenceEntrerDTO> presences;
}
