package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Professeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

 import java.util.List;
import java.util.Optional;

@Repository
public interface ProfesseurRepository extends JpaRepository<Professeur, Long> {
    
    Boolean existsByCin(String cin);

    boolean existsByPpr(String ppr);

    boolean existsByEmail(String email);

    Optional<Professeur> findByPpr(String ppr);

    List<Professeur> findByDepartementDirigee_CodeDepartement(String codeDepartement);

    @Query("""
select p from Professeur p 
join p.affectationEnseignements a 
join a.seances s 
where s.id=:seanceId
""")
    Professeur findBySeance_SeanceId(@Param("seanceId") Long seanceId);

<<<<<<< HEAD
    Optional<Professeur> findByEmail(String email);

=======
 Optional<Professeur> findByEmail(String email);
>>>>>>> 5c7b1ae2196c1252a6c997f00a357b48c945cd72
}
