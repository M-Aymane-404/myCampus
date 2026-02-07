package com.mycampus.backend.repositorys;

 import com.mycampus.backend.entites.Role;
import com.mycampus.backend.enumerations.RolesCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByNomRole(RolesCode nomRole);
    
    boolean existsByNomRole(RolesCode nomRole);

}
