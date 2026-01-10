package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.AffectationEnseignement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AffectationEnseignementRepository extends JpaRepository<AffectationEnseignement, Long> {
    
    List<AffectationEnseignement> findByProfesseurId(Long professeurId);
    
    List<AffectationEnseignement> findByClasseId(Long classeId);
    
    List<AffectationEnseignement> findByMatiereId(Long matiereId);
    
    List<AffectationEnseignement> findByActiveTrue();
    
    List<AffectationEnseignement> findByAnneeUniversitaireAndSemestre(String anneeUniversitaire, String semestre);
}
