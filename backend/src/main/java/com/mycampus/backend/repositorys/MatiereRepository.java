package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Matiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatiereRepository extends JpaRepository<Matiere, Long> {
    
    boolean existsByCodeMatiere(String codeMatiere);
    
    List<Matiere> findByModuleId(Long moduleId);
}
