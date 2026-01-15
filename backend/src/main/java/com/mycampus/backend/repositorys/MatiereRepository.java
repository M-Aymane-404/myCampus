package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Matiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatiereRepository extends JpaRepository<Matiere, Long> {
    
    boolean existsByCodeMatiere(String codeMatiere);
    
    Optional<Matiere> findByCodeMatiere(String codeMatiere);
    
    List<Matiere> findByModuleId(Long moduleId);
    
    List<Matiere> findByModule_CodeModule(String codeModule);
}
