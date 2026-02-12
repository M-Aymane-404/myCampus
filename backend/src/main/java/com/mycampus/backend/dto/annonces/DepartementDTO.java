package com.mycampus.backend.dto.annonces;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartementDTO {
     private String codeDepartement;

     private String typeDepartement;
}
