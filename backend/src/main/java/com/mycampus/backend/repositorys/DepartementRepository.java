package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Departement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartementRepository extends JpaRepository<Departement, Long> {
    
    boolean existsByCodeDepartement(String codeDepartement);
    
    Optional<Departement> findByCodeDepartement(String codeDepartement);
}
