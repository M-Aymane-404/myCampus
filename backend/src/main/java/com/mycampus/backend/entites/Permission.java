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
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
     private String codePermission;
    private String description;
    private LocalDateTime dateCreation;


    @OneToMany(mappedBy = "permission",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<RolePermission> rolePermissions;
}
