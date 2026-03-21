package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Annonce;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.enumerations.AnnonceCibleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Long> {

    @Query("""
select a from Annonce a
left join fetch a.auteur
left join fetch a.classe c
where c.codeClasse =:codeClasse
""")
    List<Annonce> findByClasse_CodeClasse(@Param("codeClasse") String codeClasse);

    @Query("""
select a from Annonce a
left join fetch a.auteur
left join fetch a.classe
where a.auteur.id =:auteurId
""")
    List<Annonce> findByAuteur_Id(@Param("auteurId") Long auteurId);

    @Query("""
select a from Annonce a
left join fetch a.auteur
left join fetch a.classe
where a.cibleType =:cibleType
""")
    List<Annonce> findByCibleType(@Param("cibleType") AnnonceCibleType cibleType);

    @Query("""
select a from Annonce a
left join fetch a.auteur
left join fetch a.classe
""")
    List<Annonce> findAllWithAuteurAndClasse();
    


    @Query("""
SELECT a FROM Annonce a 
left join fetch a.auteur
left join fetch a.classe c
WHERE a.cibleType =:cibleType     or (c is not null and c.id = :classeId)
""")
    List<Annonce> findByCibleTypeOrClasseId(@Param("cibleType") AnnonceCibleType cibleType, @Param("classeId") Long classeId);

    @Query("""
SELECT a FROM Annonce a
left join fetch a.auteur
left join fetch a.classe
WHERE a.cibleType = 'ETUDIANTS_CLASSE' OR a.classe IN :classes""")
    List<Annonce> findGlobalAndClassAnnoncesDeProf(@Param("classes") List<Classe> classes);

    @Query("""
select a from Annonce a
left join fetch a.auteur
left join fetch a.classe
where a.id =:id
""")
    Optional<Annonce> findByAnnonce_Id(@Param("id") Long id);

    @Query("""
select a from Annonce a
left join fetch a.auteur
left join fetch a.departement
where a.cibleType = 'PROFESSEURS_DEPARTEMENT' or a.cibleType = 'PROFESSEURS_GLOBAL'
""")
    List<Annonce> findAnnoncesProfesseursGlobalEtCiblees();
}
