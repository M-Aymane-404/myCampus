package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Annonce;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.enumerations.AnnonceCibleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Long> {
    
    List<Annonce> findByClasse_CodeClasse(String codeClasse);
    
    List<Annonce> findByAuteur_Id(Long auteurId);
    
    List<Annonce> findByCibleType(AnnonceCibleType cibleType);
    

    


    @Query("SELECT a FROM Annonce a WHERE a.cibleType = 'GLOBAL' OR a.classe.id = :classeId")
    List<Annonce> findByCibleTypeOrClasseId(@Param("cibleType") AnnonceCibleType cibleType, @Param("classeId") Long classeId);

    @Query("SELECT a FROM Annonce a WHERE a.cibleType = 'GLOBAL' OR a.classe IN :classes")
    List<Annonce> findGlobalAndClassAnnonces(@Param("classes") List<Classe> classes);
}
