package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Examen;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.TypeExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamenRepository extends JpaRepository<Examen, Long> {
    List<Examen> findByMatiereId(Long matiereId);

    List<Examen> findByMatiere_CodeMatiere(String codeMatiere);

    @Query("""
        select e
        from Examen e
        where e.matiere.codeMatiere = :codeMatiere
        order by e.dateExamen desc, e.numeroExamen desc
    """)
    List<Examen> findByCodeMatiere(@Param("codeMatiere") String codeMatiere);

    @Query("""
        select e
        from Examen e
        where e.matiere.codeMatiere = :codeMatiere
          and e.typeExamen = :typeExamen
        order by e.dateExamen desc, e.numeroExamen desc
    """)
    List<Examen> findByCodeMatiereAndTypeExamen(@Param("codeMatiere") String codeMatiere,
                                                @Param("typeExamen") TypeExamen typeExamen);


    boolean existsByMatiere_CodeMatiereAndSemestreAndNumeroExamenAndTypeExamen(
            String codeMatiere, Semestre semestre, Integer numeroExamen, TypeExamen typeExamen
    );
    @Query("""
  select e
  from Examen e
  join fetch e.matiere m
  where e.id = :id
""")
    Optional<Examen> findByIdWithMatiere(@Param("id") Long id);
}
