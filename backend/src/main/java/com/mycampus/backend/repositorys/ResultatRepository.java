package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Examen;
import com.mycampus.backend.entites.Resultat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultatRepository extends JpaRepository<Resultat, Long> {

    boolean existsByEtudiantIdAndExamenId(Long etudiantId, Long examenId);
    
    boolean existsByExamenAndEtudiant(Examen examen, Etudiant etudiant);


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

    @Query("""
        select r
        from Resultat r
        where r.etudiant.id = :etudiantId
        order by r.datePublication desc
        limit 3
    """)
    List<Resultat> findTop3ByEtudiantIdOrderByDatePublicationDesc(@Param("etudiantId") Long etudiantId);

    Optional<Resultat> findByExamen_IdAndEtudiant_Id(Long examenId, Long etudiantId);

    @Query("""
    select r
    from Resultat r
    join fetch r.etudiant e
    where r.examen.id = :examenId
""")
    List<Resultat> findByExamenIdWithEtudiant(@Param("examenId") Long examenId);

    boolean existsByExamen_Id(Long examenId);

    long countByExamen_Id(Long examenId);
    
}
