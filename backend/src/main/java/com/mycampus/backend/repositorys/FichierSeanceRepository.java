package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.FichierSeance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FichierSeanceRepository extends JpaRepository<FichierSeance, Long> {

    List<FichierSeance> findBySeance_Id(Long seanceId);
}
