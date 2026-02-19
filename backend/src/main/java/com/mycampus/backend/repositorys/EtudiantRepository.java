package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

 import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    interface ProjectionDestinataireNotification {
        Long getId();
        String getNom();
        String getPrenom();
        String getEmail();
    }

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
        select e from Etudiant e
        join fetch e.classe c
        join fetch c.filiere
        where e.id = :id
    """)
    Optional<Etudiant> findByIdWithClasseAndFiliere(@Param("id") Long id);

    List<Etudiant> findAllByClasse_CodeClasse(String codeClasse);

    @Query("""
        select e.id as id, e.nom as nom, e.prenom as prenom, e.email as email
        from Etudiant e
        where e.classe.codeClasse = :codeClasse
    """)
    List<ProjectionDestinataireNotification> trouverDestinatairesNotificationParCodeClasse(@Param("codeClasse") String codeClasse);

    @Query("""
        select e.id as id, e.nom as nom, e.prenom as prenom, e.email as email
        from Etudiant e
    """)
    List<ProjectionDestinataireNotification> trouverTousDestinatairesNotification();

    boolean existsByEmail(String email);

    boolean existsByCin(String cin);

    @Query("SELECT e.apogee FROM Etudiant e")
    Set<String> findAllApogees();

    @Query("SELECT e.email FROM Etudiant e")
    Set<String> findAllEmails();


}
