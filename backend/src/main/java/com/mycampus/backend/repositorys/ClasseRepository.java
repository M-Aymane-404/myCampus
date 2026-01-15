package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Classe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClasseRepository extends JpaRepository<Classe, Long> {
    
    boolean existsByCodeClasse(String codeClasse);
    
    Optional<Classe> findByCodeClasse(String codeClasse);
    
    List<Classe> findByFiliereId(Long filiereId);
    
    List<Classe> findByFiliere_CodeFiliere(String codeFiliere);
}
