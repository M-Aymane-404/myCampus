package com.mycampus.backend.repositorys;

 import com.mycampus.backend.entites.RolePermission;
 import com.mycampus.backend.enumerations.RolesCode;
 import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {


    boolean existsByRole_NomRoleAndPermission_CodePermission(RolesCode rolesCode, String codePermission);
}
