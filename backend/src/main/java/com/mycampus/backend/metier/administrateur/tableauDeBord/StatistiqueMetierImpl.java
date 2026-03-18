package com.mycampus.backend.metier.administrateur.tableauDeBord;

import com.mycampus.backend.dto.administrateur.DonneSortie.TableauBordDTO.AlertesTableauBordDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.TableauBordDTO.StatistiquesGlobalesDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.TableauBordDTO.StatistiquesParFiliereDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.TableauBordDTO.TableauBordAdministratifDTO;
import com.mycampus.backend.entites.Filiere;
import com.mycampus.backend.repositorys.AffectationEnseignementRepository;
import com.mycampus.backend.repositorys.ClasseRepository;
import com.mycampus.backend.repositorys.EtudiantRepository;
import com.mycampus.backend.repositorys.FiliereRepository;
import com.mycampus.backend.repositorys.MatiereRepository;
import com.mycampus.backend.repositorys.ProfesseurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatistiqueMetierImpl implements StatistiqueMetier {

    private final EtudiantRepository etudiantRepository;
    private final ProfesseurRepository professeurRepository;
    private final ClasseRepository classeRepository;
    private final MatiereRepository matiereRepository;
    private final AffectationEnseignementRepository affectationEnseignementRepository;
    private final FiliereRepository filiereRepository;

    @Override
    @Transactional(readOnly = true)
    public TableauBordAdministratifDTO obtenirTableauDeBordAdministratif() {
        StatistiquesGlobalesDTO statistiquesGlobales = StatistiquesGlobalesDTO.builder()
                .totalEtudiants(etudiantRepository.count())
                .totalProfesseurs(professeurRepository.count())
                .totalClasses(classeRepository.count())
                .totalMatieres(matiereRepository.count())
                .totalAffectationsActives(affectationEnseignementRepository.countByActiveTrue())
                .build();

        AlertesTableauBordDTO alertes = AlertesTableauBordDTO.builder()
                .matieresSansProfesseur(matiereRepository.countMatieresSansProfesseurActif())
                .professeursSansAffectation(professeurRepository.countProfesseursSansAffectationActive())
                .build();

        List<StatistiquesParFiliereDTO> etudiantsParFiliere = filiereRepository.findAll()
                .stream()
                .map(this::versStatistiquesParFiliere)
                .toList();

        return TableauBordAdministratifDTO.builder()
                .statistiquesGlobales(statistiquesGlobales)
                .alertes(alertes)
                .etudiantsParFiliere(etudiantsParFiliere)
                .build();
    }

    private StatistiquesParFiliereDTO versStatistiquesParFiliere(Filiere filiere) {
        Integer totalEtudiants = etudiantRepository.countEtudiantsByCodeFiliere(filiere.getCodeFiliere());
        return StatistiquesParFiliereDTO.builder()
                .codeFiliere(filiere.getCodeFiliere())
                .filiere(filiere.getNom())
                .totalEtudiants(totalEtudiants != null ? totalEtudiants.longValue() : 0L)
                .build();
    }
}
