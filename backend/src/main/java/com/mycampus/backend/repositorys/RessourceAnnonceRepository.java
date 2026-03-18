package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.RessourceAnnonce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RessourceAnnonceRepository extends JpaRepository<RessourceAnnonce, Long> {

    List<RessourceAnnonce> findByAnnonce_IdOrderByDateUploadDesc(Long annonceId);

    Optional<RessourceAnnonce> findByIdAndAnnonce_Auteur_Email(Long id, String email);
}
