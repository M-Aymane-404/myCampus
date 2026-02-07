package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.FichierSeance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichierSeanceRepository extends JpaRepository<FichierSeance, Long> {

    List<FichierSeance> findBySeance_Id(Long seanceId);

    List<FichierSeance> findBySeance_IdAndSeance_AffectationEnseignement_Professeur_Ppr(Long seanceId, String professeurPpr);

    Optional<FichierSeance> findByIdAndSeance_AffectationEnseignement_Professeur_Ppr(Long id, String professeurPpr);

    void deleteBySeanceId(Long seanceId);
}
