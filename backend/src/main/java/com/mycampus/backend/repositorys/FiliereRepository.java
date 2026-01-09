package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FiliereRepository extends JpaRepository<Filiere, Long> {
    
    boolean existsByCodeFiliere(String codeFiliere);
    
    List<Filiere> findByDepartementId(Long departementId);
}
