package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Utilisateur;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    
    boolean existsByEmail(String email);
    
    boolean existsByCin(String cin);

    @Query("SELECT u.cin FROM Utilisateur u")
    Set<String> findAllCins();

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByTel( String tel);
}

