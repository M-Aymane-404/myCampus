package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Professeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfesseurRepository extends JpaRepository<Professeur, Long> {
    
    Boolean existsByCin(String cin);

    boolean existsByPpr(String ppr);

    boolean existsByEmail(String email);

    Professeur findByPpr(String ppr);
}
