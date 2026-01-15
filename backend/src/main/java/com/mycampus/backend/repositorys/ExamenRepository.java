package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Examen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamenRepository extends JpaRepository<Examen, Long> {
    List<Examen> findByMatiereId(Long matiereId);

    List<Examen> findByMatiere_CodeMatiere(String codeMatiere);
}
