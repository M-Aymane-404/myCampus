package com.mycampus.backend.metier.etudiant.StructureEtudiantMapper;

import com.mycampus.backend.dto.etudiant.DonneeSortie.*;
import com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote.ExamenDeReleveEtudiantDTO;
import com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote.ModuleDeReleveEtudiantDTO;
import com.mycampus.backend.dto.etudiant.DonneeSortie.ReleveDeNote.ReleveDeNoteEtudiantParSessionDTO;
import com.mycampus.backend.entites.*;
import com.mycampus.backend.entites.Module;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.enumerations.StatusAbsence;
import com.mycampus.backend.enumerations.TypeExamen;
import com.mycampus.backend.enumerations.TypeSeance;
import com.mycampus.backend.exceptions.EtudiantException;
import com.mycampus.backend.mappers.etudiant.StructureEtudiantMapper;
import com.mycampus.backend.repositorys.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final ResultatRepository resultatRepository;
    private final AffectationEnseignementRepository affectationEnseignementRepository;
    private final ProfesseurRepository professeurRepository;
    private final StructureEtudiantMapper mapper;

    @Override
    public EtudiantSortieEtudiantDTO trouverEtudiantConnecter(Long id) throws EtudiantException {


        if(id == null){
            throw  new EtudiantException("id est obli");
        }



        return null;
    }


    @Override
    @Transactional(readOnly = true)
    public List<ModuleSortieEtudiantDTO> trouverModulesDEtudiantConnecter(Long id) throws EtudiantException {
        if(id == null){
            throw  new EtudiantException("id est obligatoire");
        }

        List<Module> modules = moduleRepository.findByEtudiantId(id);

        return mapper.DuListeModuleVersListModuleSortieEtudiantDTO(modules);

    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public List<MatiereSortieEtudiantDTO> trouverMatiereParCodeModule(Long EtudiantId , String codeModule) throws EtudiantException {
        if(codeModule == null){
            throw  new EtudiantException("code Module  est obligatoire");
        }

        List<Matiere> matieres = matiereRepository.findByModule_CodeModule(codeModule);
        Classe classe = classeRepository.findByEtudiant_idEtudiant(EtudiantId);

        if(matieres == null){
            return List.of();
        }else {

            return matieres.stream().map(matier -> {
                MatiereSortieEtudiantDTO matiereSortieEtudiantDTO = mapper.DuMatiereVersMatiereSortieEtudiantDTO(matier);
                AffectationEnseignement affectationEnseignement = affectationEnseignementRepository.findByMatiere_CodeMatiereAndClasse_CodeClasse(matier.getCodeMatiere(), classe.getCodeClasse());
                if (affectationEnseignement != null) {
                    matiereSortieEtudiantDTO.setProfesseurDeMatiere(affectationEnseignement.getProfesseur().getNom() + affectationEnseignement.getProfesseur().getPrenom());
                } else {
                    matiereSortieEtudiantDTO.setProfesseurDeMatiere(null);
                }
                return matiereSortieEtudiantDTO;
            }).toList();

        }
    }


    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public List<FichierSeanceSortieEtudiantDTO> trouverRessourceD_uneSeance(Long seanceId){

            List<FichierSeance> ressources = fichierSeanceRepository.findBySeance_Id(seanceId);

            return mapper.DuListeFichierSeanceVersListeFichierSeanceSortieEtudiantDTO(ressources);
    }




    @Override
    @Transactional(readOnly = true)
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
                    .filter(p -> p.getSeance().getDate().equals(date))
                    .toList();
        }
        if(presences == null){
            return List.of();
        }else {

            return presences.stream().map(presence -> {
                PresenceSortieEtudiantDTO presenceSortieEtudiantDTO = mapper.DuPresenceVersPresenceSortieEtudiantDTO(presence);
                Seance seanceDuPresence = seanceRepository.findByPresence_presenceId(presence.getId());
                Professeur prof = professeurRepository.findBySeance_SeanceId(seanceDuPresence.getId());

                MiniSeanceSortieEtudiantDTO seance = new MiniSeanceSortieEtudiantDTO();

                seance.setDate(seanceDuPresence.getDate());
                seance.setSalle(seanceDuPresence.getSalle());
                seance.setType(seanceDuPresence.getType());
                seance.setProf(prof.getNom() + " " + prof.getPrenom());

                presenceSortieEtudiantDTO.setSeance(seance);
                return presenceSortieEtudiantDTO;
            }).toList();
        }

    }




    @Override
    @Transactional(readOnly = true)
    public ReleveDeNoteEtudiantParSessionDTO trouverLeReleverDeNoteParSessionDuEtudiant(Long idEtudiant, Semestre semestre)
            throws EtudiantException {

        if (idEtudiant == null) throw new EtudiantException("id d'etudiant est obligatoire");
        if (semestre == null) throw new EtudiantException("semestre est obligatoire");

        List<Module> modules = moduleRepository.findByEtudiantIdEtSemester(idEtudiant, semestre);

        List<ModuleDeReleveEtudiantDTO> modulesDto = modules.stream()
                .map(mapper::DuModuleVersModuleDeReleveEtudiantDTO)
                .toList();

        List<Long> examenIds = modulesDto.stream()
                .filter(m -> m.getMatieres() != null)
                .flatMap(m -> m.getMatieres().stream())
                .filter(mat -> mat.getExamens() != null)
                .flatMap(mat -> mat.getExamens().stream())
                .map(ExamenDeReleveEtudiantDTO::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Resultat> resultatByExamenId = examenIds.isEmpty()
                ? Map.of()
                : resultatRepository.findAllByEtudiantIdAndExamenIds(idEtudiant, examenIds).stream()
                .collect(Collectors.toMap(
                        r -> r.getExamen().getId(),
                        r -> r,
                        (a, b) -> { throw new IllegalStateException("Doublon résultat pour examen_id=" + a.getExamen().getId()); }
                ));


        modulesDto.forEach(moduleDto -> {
            if (moduleDto.getMatieres() == null) return;

            moduleDto.getMatieres().forEach(matiereDto -> {
                if (matiereDto.getExamens() == null) return;

                // 1) Remplir notes + copie
                matiereDto.getExamens().forEach(examenDto -> {
                    Resultat r = resultatByExamenId.get(examenDto.getId());
                    if (r != null) {
                        examenDto.setNote(r.getNote());
                        examenDto.setCopieEtudiantPath(r.getCopieEtudiantPath());
                    } else {
                        examenDto.setNote(null);
                        examenDto.setCopieEtudiantPath(null);
                    }
                });

                // 2) Calcul moyenne (règle: si rattrapage existe et noté => moyenne = rattrapage,
                // sinon moyenne = average des NORMAL notés, sinon null)
                Double noteRattrapage = matiereDto.getExamens().stream()
                        .filter(e -> e.getTypeExamen() == TypeExamen.RATTRAPAGE)
                        .map(ExamenDeReleveEtudiantDTO::getNote)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);

                if (noteRattrapage != null) {
                    matiereDto.setMoyenne(noteRattrapage);
                } else {

                    double moyenneNormal = matiereDto.getExamens().stream()
                            .filter(e -> e.getTypeExamen() == TypeExamen.NORMAL)
                            .map(ExamenDeReleveEtudiantDTO::getNote)
                            .filter(Objects::nonNull)
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(Double.NaN);

                    matiereDto.setMoyenne(Double.isNaN(moyenneNormal) ? null : moyenneNormal);
                    matiereDto.setValidee(
                            matiereDto.getMoyenne() != null && matiereDto.getMoyenne() >= 10
                    );


                }
            });
        });

        return ReleveDeNoteEtudiantParSessionDTO.builder()
                .semestre(semestre)
                .modules(modulesDto)
                .build();

    }











}
