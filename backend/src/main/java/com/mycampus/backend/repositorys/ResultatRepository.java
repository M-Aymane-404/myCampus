package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Resultat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultatRepository extends JpaRepository<Resultat, Long> {

    boolean existsByEtudiantIdAndExamenId(Long etudiantId, Long examenId);

}
