package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    
    boolean existsByCodeModule(String codeModule);
    
    Optional<Module> findByCodeModule(String codeModule);
    
    List<Module> findByFiliereId(Long filiereId);
    
    List<Module> findByFiliere_CodeFiliere(String codeFiliere);
}
