package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Professeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClasseRepository extends JpaRepository<Classe, Long> {
    
    boolean existsByCodeClasse(String codeClasse);
    
    Optional<Classe> findByCodeClasse(String codeClasse);
    
    List<Classe> findByFiliereId(Long filiereId);
    
    List<Classe> findByFiliere_CodeFiliere(String codeFiliere);

    @Query("""
select c from Classe c
join c.etudiants e
where e.id =:etudiant_id
""")
    Classe findByEtudiant_idEtudiant(@Param("etudiant_id") Long etudiant_id);

    @Query("""
SELECT DISTINCT c
FROM Classe c
JOIN c.affectationEnseignements ae
WHERE ae.professeur = :professeur
""")
    List<Classe> findClassesByProfesseur(@Param("professeur") Professeur professeur);

}
