package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Presence;
import com.mycampus.backend.entites.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PresenceRepository extends JpaRepository<Presence, Long> {

    List<Presence> findByEtudiant_Id(Long id);
    
    boolean existsBySeanceAndEtudiant(Seance seance, Etudiant etudiant);
}
