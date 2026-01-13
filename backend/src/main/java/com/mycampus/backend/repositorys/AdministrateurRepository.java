package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Administrateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministrateurRepository extends JpaRepository<Administrateur, Long> {
    
    boolean existsByPpr(String ppr);
    
    Administrateur findByPpr(String ppr);
    boolean existsByCin(String cin);

    boolean existsByEmail(String email);
}
