package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Classe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClasseRepository extends JpaRepository<Classe, Long> {
    
    boolean existsByCodeClasse(String codeClasse);
    
    List<Classe> findByFiliereId(Long filiereId);
}
