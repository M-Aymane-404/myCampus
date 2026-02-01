package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Resultat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultatRepository extends JpaRepository<Resultat, Long> {

    boolean existsByEtudiantIdAndExamenId(Long etudiantId, Long examenId);


    @Query("""
        select r
        from Resultat r
        where r.etudiant.id = :etudiantId
          and r.examen.id in :examenIds
    """)
    List<Resultat> findAllByEtudiantIdAndExamenIds(
            @Param("etudiantId") Long etudiantId,
            @Param("examenIds") List<Long> examenIds
    );
}
