package com.mycampus.backend.repositorys;

 import com.mycampus.backend.entites.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    Optional<Permission> findByCodePermission(String codePermission);
    
    boolean existsByCodePermission(String codePermission);

}
