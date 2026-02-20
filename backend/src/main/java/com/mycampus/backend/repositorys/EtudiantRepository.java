package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

 import java.util.List;
import java.util.Optional;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    List<Etudiant> findByClasseId(Long classeId);
    
    Optional<Etudiant> findByApogee(String apogee);
    
    boolean existsByApogee(String apogee);

    List<Etudiant> findByClasse_CodeClasse(String codeClasse);

    @Query("""
   select count(e)
   from Etudiant e
   join e.classe c
   join c.filiere f
   join f.departement d
   where d.codeDepartement = :code
""")
    Integer countEtudiantsByCodeDepartement(@Param("code") String codeDepartement);

    @Query(
            """
    select count (e)
    from Etudiant e
    join e.classe c
    join c.filiere f
    where f.codeFiliere = :code
"""
    )
    Integer countEtudiantsByCodeFiliere(@Param("code") String codeFiliere);

    Optional<Etudiant> findByEmail(String email);


    @Query("""
        select e from Etudiant e
        join fetch e.classe c
        join fetch c.filiere
        where e.email = :email
    """)
    Optional<Etudiant> findByEmailWithClasseAndFiliere(@Param("email") String email);


    @Query("""
select count(e) > 0 from Etudiant e
join   e.classe c
join   c.affectationEnseignements a
join   a.seances s
where s.id = :seanceId and e.id = :etudiantId


""")
    boolean TrouverEtudiantExistenClasse(@Param("seanceId")Long seanceId,@Param("etudiantId") Long etudiantId);
}
