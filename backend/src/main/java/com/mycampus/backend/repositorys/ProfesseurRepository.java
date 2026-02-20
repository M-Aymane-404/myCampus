package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Professeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

 import java.util.List;
import java.util.Optional;

@Repository
public interface ProfesseurRepository extends JpaRepository<Professeur, Long> {
    interface ProjectionDestinataireNotification {
        Long getId();
        String getNom();
        String getPrenom();
        String getEmail();
    }
    
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

 Optional<Professeur> findByEmail(String email);

    List<Professeur> findAllByDepartementOuTravail_CodeDepartement(String codeDepartement);

    @Query("""
        select p.id as id, p.nom as nom, p.prenom as prenom, p.email as email
        from Professeur p
        where p.departementOuTravail.codeDepartement = :codeDepartement
    """)
    List<ProjectionDestinataireNotification> trouverDestinatairesNotificationParCodeDepartement(@Param("codeDepartement") String codeDepartement);

    @Query("""
        select p.id as id, p.nom as nom, p.prenom as prenom, p.email as email
        from Professeur p
    """)
    List<ProjectionDestinataireNotification> trouverTousDestinatairesNotification();
}
