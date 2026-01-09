package com.mycampus.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
public class DepartementDTO {
    
    private final Long id;
    private final String codeDepartement;
    private final String typeDepartement;
    private final String description;
}
