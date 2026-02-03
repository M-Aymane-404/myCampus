package com.mycampus.backend.entites;

import com.mycampus.backend.enumerations.RolesCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {


     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private RolesCode nomRole;
    private String description;
    private Boolean actif;
    private LocalDateTime dateCreation;


    @OneToMany(mappedBy = "role",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<UtilisateurRole> utilisateurRoles;


    @OneToMany(mappedBy = "role",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<RolePermission> rolePermissions;

}
