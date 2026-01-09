package com.mycampus.backend.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class DepartementDTO {
    
    private final Long id;
    private final String codeDepartement;
    private final String typeDepartement;
    private final String description;
}
