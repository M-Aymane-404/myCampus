package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.entites.Seance;
import com.mycampus.backend.enumerations.TypeSeance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {


    @Query("""
select distinct s from Seance s
join s.affectationEnseignement a
where a.classe.codeClasse =:codeClasse and a.matiere.codeMatiere =:codeMatiere
""")
    List<Seance> findByClasse_CodeClasseAndMatiere_CodeMatiere(@Param("codeClasse") String codeClasse, @Param("codeMatiere") String codeMatiere);

    @Query("""
select distinct s from Seance s
join s.affectationEnseignement a
where a.classe.codeClasse =:codeClasse and a.matiere.codeMatiere =:codeMatiere and s.type=:type
""")
    List<Seance> findByClasse_CodeClasseAndMatiere_CodeMatiereAndSeance_typeSeance(@Param("codeClasse") String codeClasse, @Param("codeMatiere") String codeMatiere,@Param("type") TypeSeance typeSeance);

    @Query("""
select s from Seance s 
join s.presences p 
where p.id =:presenceId 
""")
Seance findByPresence_presenceId(@Param("presenceId") Long presenceId);


}
