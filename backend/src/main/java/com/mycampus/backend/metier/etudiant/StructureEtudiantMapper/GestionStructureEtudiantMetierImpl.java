package com.mycampus.backend.metier.etudiant.StructureEtudiantMapper;

import com.mycampus.backend.dto.etudiant.DonneeSortie.*;
import com.mycampus.backend.entites.*;
import com.mycampus.backend.entites.Module;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.StatusAbsence;
import com.mycampus.backend.enumerations.TypeSeance;
import com.mycampus.backend.exceptions.EtudiantException;
import com.mycampus.backend.mappers.etudiant.StructureEtudiantMapper;
import com.mycampus.backend.repositorys.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GestionStructureEtudiantMetierImpl implements GestionStructureEtudiantMetier {
     private final EtudiantRepository etudiantRepository;
    private final ModuleRepository moduleRepository;
    private final MatiereRepository matiereRepository;
    private final SeanceRepository seanceRepository;
    private final ClasseRepository classeRepository;
    private final FichierSeanceRepository fichierSeanceRepository;
    private final PresenceRepository presenceRepository;
    private final StructureEtudiantMapper mapper;

    @Override
    public EtudiantSortieEtudiantDTO trouverEtudiantConnecter(Long id) throws EtudiantException {


        if(id == null){
            throw  new EtudiantException("id est obli");
        }



        return null;
    }


    @Override
    public List<ModuleSortieEtudiantDTO> trouverModulesDEtudiantConnecter(Long id) throws EtudiantException {
        if(id == null){
            throw  new EtudiantException("id est obligatoire");
        }

        List<Module> modules = moduleRepository.findByEtudiantId(id);

        return mapper.DuListeModuleVersListModuleSortieEtudiantDTO(modules);

    }

    @Override
    public List<ModuleSortieEtudiantDTO> trouverModulesDEtudiantConnecterParSemester(Long id, Semestre semestre) throws EtudiantException {
        if(id == null){
            throw  new EtudiantException("id est obligatoire");
        }
        if(semestre == null){
            List<Module> modules = moduleRepository.findByEtudiantId(id);
            return mapper.DuListeModuleVersListModuleSortieEtudiantDTO(modules);
        }else {
            List<Module> modules = moduleRepository.findByEtudiantIdEtSemester(id,semestre);
            return mapper.DuListeModuleVersListModuleSortieEtudiantDTO(modules);
        }
    }

    @Override
    public List<MatiereSortieEtudiantDTO> trouverMatiereParCodeModule(Long EtudiantId , String codeModule) throws EtudiantException {
        if(codeModule == null){
            throw  new EtudiantException("code Module  est obligatoire");
        }

        List<Matiere> matieres = matiereRepository.findByModule_CodeModule(codeModule);
        Classe classe = classeRepository.findByEtudiant_idEtudiant(EtudiantId);

        return mapper.DuListeMatiereVersListeMatiereSortieEtudiantDTO(matieres,classe.getCodeClasse());
    }


    @Override
    public List<SeanceSortieEtudiantDTO> trouverSeanceParCodeMatiereEtFiltrerParTypeSeanceSiExist(Long etudiantId, String codeMatiere, TypeSeance typeSeance) throws EtudiantException {
        if(codeMatiere == null){
            throw  new EtudiantException("code matière est obligatoire");
        }

        if (etudiantId == null){
            throw  new EtudiantException("id d'étudiant est obligatoire");
        }
        Classe classe = classeRepository.findByEtudiant_idEtudiant(etudiantId);

        List<Seance> seances;
        if (typeSeance == null){
            seances = seanceRepository.findByClasse_CodeClasseAndMatiere_CodeMatiere(classe.getCodeClasse(), codeMatiere);

        }else {
            seances = seanceRepository.findByClasse_CodeClasseAndMatiere_CodeMatiereAndSeance_typeSeance(classe.getCodeClasse(), codeMatiere, typeSeance);
        }
        return mapper.DuListeSeanceVersListeSeanceSortieEtudiantDTO(seances);

    }

    @Override
    public List<FichierSeanceSortieEtudiantDTO> trouverRessourceD_uneSeance(Long seanceId){

            List<FichierSeance> ressources = fichierSeanceRepository.findBySeance_Id(seanceId);

            return mapper.DuListeFichierSeanceVersListeFichierSeanceSortieEtudiantDTO(ressources);
    }




    @Override
    public List<PresenceSortieEtudiantDTO> trouverToutPresenceDuEtudiantConnecter(Long IdEtudiant, StatusAbsence status, LocalDate date) throws EtudiantException {
        if(IdEtudiant == null){
            throw  new EtudiantException("id d'étudiant est obligatoire");

        }

        List<Presence> presences = presenceRepository.findByEtudiant_Id(IdEtudiant);

        if (status != null) {
            presences = presences.stream()
                    .filter(p -> p.getStatus() == status)
                    .toList();
        }

        if (date != null) {
            presences = presences.stream()
                    .filter(p -> p.getSeance().getDate() == date)
                    .toList();
        }
        return mapper.DuListePresenceVersListePresenceSortieEtudiantDTO(presences);
    }









}
