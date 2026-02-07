package com.mycampus.backend.repositorys;

 import com.mycampus.backend.entites.UtilisateurRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UtilisateurRoleRepository extends JpaRepository<UtilisateurRole, Long> {
    

}
