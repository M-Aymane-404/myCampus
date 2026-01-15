package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FiliereRepository extends JpaRepository<Filiere, Long> {
    
    boolean existsByCodeFiliere(String codeFiliere);
    
    Optional<Filiere> findByCodeFiliere(String codeFiliere);
    
    List<Filiere> findByDepartementId(Long departementId);
    
    List<Filiere> findByDepartement_CodeDepartement(String codeDepartement);
}
