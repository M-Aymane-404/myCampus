package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

 import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    
    boolean existsByEmail(String email);
    
    //long countByEnabledTrue();

    boolean existsByCin(String cin);

    boolean existsByTel(String tel);


    Optional<Utilisateur> findByEmail(String email);
}
