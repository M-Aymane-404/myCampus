package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.AffectationEnseignement;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Matiere;
import com.mycampus.backend.entites.Professeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AffectationEnseignementRepository extends JpaRepository<AffectationEnseignement, Long> {
    
   // List<AffectationEnseignement> findByProfesseurId(Long professeurId);
    
    List<AffectationEnseignement> findByProfesseur_Ppr(String professeurPpr);
    
    List<AffectationEnseignement> findByClasseId(Long classeId);
    
   // List<AffectationEnseignement> findByClasse_Id(Long classeId);
    
    List<AffectationEnseignement> findByMatiereId(Long matiereId);
    
    //List<AffectationEnseignement> findByMatiere_Id(Long matiereId);
    
    List<AffectationEnseignement> findByActiveTrue();
    
  //  List<AffectationEnseignement> findByAnneeUniversitaireAndSemestre(String anneeUniversitaire, String semestre);

    List<AffectationEnseignement> findByMatiere_CodeMatiere(String codeMatiere);

    List<AffectationEnseignement> findByClasse_CodeClasse(String codeClasse);

    AffectationEnseignement findByMatiere_CodeMatiereAndClasse_CodeClasse(String codeMatiere, String codeClasse);


    Optional<AffectationEnseignement> findByIdAndProfesseur_Ppr(Long id,  String professeurPpr);

    boolean existsByClasseAndMatiereAndProfesseur(Classe classe, Matiere matiere, Professeur professeur);

   /*Optional<AffectationEnseignement>
    findByActiveTrueAndProfesseur_PprAndClasse_CodeClasseAndMatiere_CodeMatiere(
            String ppr,
            String codeClasse,
            String codeMatiere
    );
*/
    //new test
   @Query("""
        select count(ae) > 0
        from AffectationEnseignement ae
        where ae.professeur.id = :profId
          and ae.classe.id = :classeId
    """)
   boolean existsByProfesseurIdAndClasseId(@Param("profId") Long profId,
                                           @Param("classeId") Long classeId);




    Optional<AffectationEnseignement>
    findByActiveTrueAndProfesseur_PprAndClasse_CodeClasseAndMatiere_CodeMatiereAndAnneeUniversitaire(
            String ppr,
            String codeClasse,
            String codeMatiere,
            String anneeUniversitaire
    );


    @Query("""
        select ae from AffectationEnseignement ae
        join fetch ae.professeur p
        join fetch ae.classe c
        join fetch ae.matiere m
        where ae.active = true
          and p.ppr = :ppr
    """)
    List<AffectationEnseignement> findActivesParProfesseurAvecDetails(@Param("ppr") String ppr);

    @Query("""
        select ae
        from AffectationEnseignement ae
        where ae.active = true
          and ae.professeur.ppr = :ppr
          and ae.classe.codeClasse = :codeClasse
          and ae.matiere.codeMatiere = :codeMatiere
    """)
    List<AffectationEnseignement> findActiveByProfClasseMatiere(@Param("ppr") String ppr,
                                                                @Param("codeClasse") String codeClasse,
                                                                @Param("codeMatiere") String codeMatiere);


    List<AffectationEnseignement> findByProfesseur_PprAndActiveTrue(String ppr);


}

