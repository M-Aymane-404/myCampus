package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Professeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfesseurRepository extends JpaRepository<Professeur, Long> {
    
    Boolean existsByCin(String cin);

    boolean existsByPpr(String ppr);

    boolean existsByEmail(String email);

    Optional<Professeur> findByPpr(String ppr);

    List<Professeur> findByDepartementDirigee_CodeDepartement(String codeDepartement);
}
