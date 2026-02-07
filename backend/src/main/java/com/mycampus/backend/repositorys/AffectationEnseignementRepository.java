package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.AffectationEnseignement;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Matiere;
import com.mycampus.backend.entites.Professeur;
import org.springframework.data.jpa.repository.JpaRepository;
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

<<<<<<< HEAD

    Optional<AffectationEnseignement> findByIdAndProfesseur_Ppr(Long id,  String professeurPpr);
}
=======
    boolean existsByClasseAndMatiereAndProfesseur(Classe classe, Matiere matiere, Professeur professeur);
    
 }
>>>>>>> bb5ae35b10839ffff3946e8743d76bb27d07d14e
