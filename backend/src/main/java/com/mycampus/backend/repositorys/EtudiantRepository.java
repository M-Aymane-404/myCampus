package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    List<Etudiant> findByClasseId(Long classeId);
    Etudiant findByApogee(String apogee);
    boolean existsByApogee(String apogee);
}
