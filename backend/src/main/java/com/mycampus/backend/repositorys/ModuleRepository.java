package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Module;
import com.mycampus.backend.enumerations.Semestre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    
    boolean existsByCodeModule(String codeModule);
    
    Optional<Module> findByCodeModule(String codeModule);
    
    List<Module> findByFiliereId(Long filiereId);
    
    List<Module> findByFiliere_CodeFiliere(String codeFiliere);

    Integer countByFiliere_CodeFiliere(String filiereCodeFiliere);

@Query("""
select distinct m from Module m
join m.filiere f
join f.classes c
join c.etudiants e
where e.id =:etudiantId

""")
    List<Module> findByEtudiantId(@Param("etudiantId") Long etudiantId);


    @Query("""
select distinct m from Module m
join m.filiere f
join f.classes c
join c.etudiants e
where e.id =:etudiantId and m.semestre =:semestre

""")
    List<Module> findByEtudiantIdEtSemester(@Param("etudiantId") Long etudiantId,@Param("semestre") Semestre semestre);
}
