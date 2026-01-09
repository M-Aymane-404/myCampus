package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    
    boolean existsByCodeModule(String codeModule);
    
    List<Module> findByClasseId(Long classeId);
}
