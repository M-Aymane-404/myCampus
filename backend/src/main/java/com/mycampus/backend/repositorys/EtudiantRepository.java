package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    List<Etudiant> findByClasseId(Long classeId);
    
    Optional<Etudiant> findByApogee(String apogee);
    
    boolean existsByApogee(String apogee);

    List<Etudiant> findByClasse_CodeClasse(String codeClasse);
}
