package com.mycampus.backend.repositorys;

import com.mycampus.backend.dto.professeur.DonneeSortie.presences.LignePresenceSortieDTO;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Presence;
import com.mycampus.backend.entites.Seance;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PresenceRepository extends JpaRepository<Presence, Long> {

    List<Presence> findByEtudiant_Id(Long id);
    
    List<Presence> findByEtudiantId(Long etudiantId);
    
    boolean existsBySeanceAndEtudiant(Seance seance, Etudiant etudiant);


    @EntityGraph(attributePaths = {"etudiant"})

    List<Presence> findBySeance_IdAndSeance_AffectationEnseignement_Professeur_Ppr(Long seanceId, String professeurPpr);

    Optional<Presence> findBySeance_IdAndEtudiant_Apogee(Long seanceId, String apogee);

    Optional<Presence> findByIdAndEtudiant_Id(Long id, Long etudiantId);

   /* @Query("""
        select new com.mycampus.backend.dto.professeur.DonneeSortie.presences.LignePresenceSortieDTO(
            p.id,
            e.id,
            e.apogee,
            e.nom,
            e.prenom,
            p.status,
            p.justifiee,
            p.motif,
            p.dateDeclaration
        )
        from Presence p
        join p.etudiant e
        where p.seance.id = :seanceId
          and p.seance.affectationEnseignement.professeur.ppr = :professeurPpr
    """)
    List<LignePresenceSortieDTO> listerLignesPresencePourSeance(@Param("seanceId") Long seanceId,
                                                                @Param("professeurPpr") String professeurPpr);*/

}
