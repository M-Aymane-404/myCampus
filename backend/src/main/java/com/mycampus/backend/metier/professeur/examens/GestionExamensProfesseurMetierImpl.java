package com.mycampus.backend.metier.professeur.examens;

import com.mycampus.backend.dto.professeur.DonneeEntrer.examens.ExamenCreationEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.examens.ExamenModificationEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.examens.*;
import com.mycampus.backend.entites.AffectationEnseignement;
import com.mycampus.backend.entites.Examen;
import com.mycampus.backend.enumerations.EtatExamen;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.exceptions.NotesProfesseurException;
import com.mycampus.backend.metier.professeur.StockageFichiersService;
import com.mycampus.backend.repositorys.AffectationEnseignementRepository;
import com.mycampus.backend.repositorys.ExamenRepository;
import com.mycampus.backend.repositorys.ResultatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GestionExamensProfesseurMetierImpl  implements GestionExamensProfesseurMetier {

    private final AffectationEnseignementRepository affectationRepo;
    private final ExamenRepository examenRepo;
    private final ResultatRepository resultatRepo;
    private final StockageFichiersService stockage;

    private void validateCreationDto(ExamenCreationEntrerDTO dto) {
        if (dto == null) throw new NotesProfesseurException("DTO obligatoire.");
        if (dto.getDateExamen() == null) throw new NotesProfesseurException("dateExamen obligatoire.");
        if (dto.getNumeroExamen() == null) throw new NotesProfesseurException("numeroExamen obligatoire.");
        if (dto.getTypeExamen() == null) throw new NotesProfesseurException("typeExamen obligatoire.");
        if (dto.getDuree() == null) throw new NotesProfesseurException("duree obligatoire.");
        if (dto.getNumeroExamen() <= 0) throw new NotesProfesseurException("numeroExamen doit etre > 0.");
        if (dto.getDuree() <= 0) throw new NotesProfesseurException("duree doit etre > 0.");
    }

    private AffectationEnseignement requireAffectationUniqueActive(String ppr, String codeClasse, String codeMatiere) {
        if (ppr == null || ppr.isBlank()) throw new NotesProfesseurException("PPR obligatoire.");
        if (codeClasse == null || codeClasse.isBlank()) throw new NotesProfesseurException("codeClasse obligatoire.");
        if (codeMatiere == null || codeMatiere.isBlank()) throw new NotesProfesseurException("codeMatiere obligatoire.");

        List<AffectationEnseignement> list = affectationRepo.findActiveByProfClasseMatiere(ppr, codeClasse, codeMatiere);
        if (list.isEmpty()) throw new NotesProfesseurException("Accès refusé: affectation inexistante/inactive.");
        if (list.size() > 1) throw new NotesProfesseurException("Contexte ambigu: plusieurs affectations actives. Préciser l'année.");
        return list.get(0);
    }

    private Examen requireExamen(Long examenId) {
        return examenRepo.findById(examenId).orElseThrow(() -> new NotesProfesseurException("Examen introuvable."));
    }

    private void validateMatiere(String codeMatiere, Examen examen) {
        String real = examen.getMatiere() != null ? examen.getMatiere().getCodeMatiere() : null;
        if (real == null || !real.equals(codeMatiere)) {
            throw new NotesProfesseurException("Incohérence: examen != matière demandée.");
        }
    }

    private ExamenSortieDTO toSortieDTO(Examen e) {
        return ExamenSortieDTO.builder()
                .idExamen(e.getId())
                .codeMatiere(e.getMatiere().getCodeMatiere())
                .nomMatiere(e.getMatiere().getNom())
                .semestre(e.getSemestre())
                .numeroExamen(e.getNumeroExamen())
                .typeExamen(e.getTypeExamen())
                .dateExamen(e.getDateExamen())
                .duree(e.getDuree())
                .etat(e.getEtat())
                .corrigeDisponible(e.getCorrigePath() != null && !e.getCorrigePath().isBlank())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FiltreExamensSortieDTO chargerFiltresExamens(String ppr) {
        List<AffectationEnseignement> affectations = affectationRepo.findByProfesseur_PprAndActiveTrue(ppr);

        Map<String, ClasseFiltreExamensDTO> map = new HashMap<>();

        for (AffectationEnseignement ae : affectations) {
            if (ae.getClasse() == null || ae.getMatiere() == null) continue;

            String codeClasse = ae.getClasse().getCodeClasse();
            ClasseFiltreExamensDTO classe = map.computeIfAbsent(codeClasse, cc ->
                    ClasseFiltreExamensDTO.builder()
                            .codeClasse(cc)
                            .nomClasse(ae.getClasse().getNom())
                            .matieres(new ArrayList<>())
                            .build()
            );

            String codeMat = ae.getMatiere().getCodeMatiere();
            boolean exists = classe.getMatieres().stream().anyMatch(m -> codeMat.equals(m.getCodeMatiere()));
            if (!exists) {
                classe.getMatieres().add(MatiereFiltreExamensDTO.builder()
                        .codeMatiere(codeMat)
                        .nomMatiere(ae.getMatiere().getNom())
                        .build());
            }
        }

        List<ClasseFiltreExamensDTO> classes = map.values().stream()
                .sorted(Comparator.comparing(ClasseFiltreExamensDTO::getCodeClasse, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        return FiltreExamensSortieDTO.builder().classes(classes).build();
    }
    @Override
    @Transactional(readOnly = true)
    public List<ExamenSortieDTO> listerExamens(String ppr, String codeClasse, String codeMatiere) {
        // Vérification : le prof est bien affecté à cette classe/matière
        requireAffectationUniqueActive(ppr, codeClasse, codeMatiere);

        return examenRepo.findByCodeMatiere(codeMatiere)
                .stream()
                .map(this::toSortieDTO)
                .collect(Collectors.toList());
    }



    @Override
    @Transactional
    public ExamenSortieDTO creerExamen(String ppr, String codeClasse, String codeMatiere, ExamenCreationEntrerDTO dto) {
        validateCreationDto(dto);
        AffectationEnseignement ae = requireAffectationUniqueActive(ppr, codeClasse, codeMatiere);

        Semestre semestre = dto.getSemestre();
        if (semestre == null) {
            try {
                semestre = ae.getMatiere().getModule().getSemestre();
            } catch (Exception ignored) {
                semestre = null;
            }
        }
        if (semestre == null) {
            throw new NotesProfesseurException("semestre obligatoire (introuvable via le module de la matiere).");
        }

        boolean exists = examenRepo.existsByMatiere_CodeMatiereAndSemestreAndNumeroExamenAndTypeExamen(
                codeMatiere, semestre, dto.getNumeroExamen(), dto.getTypeExamen()
        );
        if (exists) throw new NotesProfesseurException("Examen déjà existant (même matière/semestre/numéro/type).");

        Examen examen = Examen.builder()
                .matiere(ae.getMatiere())
                .semestre(semestre)
                .dateExamen(dto.getDateExamen())
                .numeroExamen(dto.getNumeroExamen())
                .typeExamen(dto.getTypeExamen())
                .duree(dto.getDuree())
                .etat(EtatExamen.PROGRAMME)
                .build();

        return toSortieDTO(examenRepo.save(examen));
    }
    @Override
    @Transactional
    public ExamenSortieDTO modifierExamen(String ppr, Long examenId, ExamenModificationEntrerDTO dto) {

        Examen examen = examenRepo.findByIdWithMatiere(examenId)
                .orElseThrow(() -> new NotesProfesseurException("Examen introuvable."));


        boolean affecte = affectationRepo.findActivesParProfesseurAvecDetails(ppr).stream()
                .anyMatch(ae -> ae.getMatiere() != null
                        && examen.getMatiere().getCodeMatiere().equals(ae.getMatiere().getCodeMatiere()));

        if (!affecte) {
            throw new NotesProfesseurException("Accès refusé: vous n'êtes pas affecté à cette matière.");
        }

        if (dto.getDateExamen() != null) examen.setDateExamen(dto.getDateExamen());
        if (dto.getDuree() != null) examen.setDuree(dto.getDuree());
        if (dto.getEtat() != null) examen.setEtat(dto.getEtat());

        Examen saved = examenRepo.save(examen);

        // mapping safe (matiere join-fetch => pas de lazy)
        return ExamenSortieDTO.builder()
                .idExamen(saved.getId())
                .codeMatiere(saved.getMatiere().getCodeMatiere())
                .nomMatiere(saved.getMatiere().getNom() == null ? "" : saved.getMatiere().getNom())
                .semestre(saved.getSemestre())
                .numeroExamen(saved.getNumeroExamen())
                .typeExamen(saved.getTypeExamen())
                .dateExamen(saved.getDateExamen())
                .duree(saved.getDuree())
                .etat(saved.getEtat())
                .corrigeDisponible(saved.getCorrigePath() != null && !saved.getCorrigePath().isBlank())
                .build();
    }


    @Transactional
    @Override
    public void supprimerExamen(String ppr, Long examenId) {

        Examen examen = examenRepo.findByIdWithMatiere(examenId)
                .orElseThrow(() -> new NotesProfesseurException("Examen introuvable."));

        boolean affecte = affectationRepo.findActivesParProfesseurAvecDetails(ppr).stream()
                .anyMatch(ae -> ae.getMatiere() != null
                        && examen.getMatiere().getCodeMatiere().equals(ae.getMatiere().getCodeMatiere()));

        if (!affecte) {
            throw new NotesProfesseurException("Accès refusé: vous n'êtes pas affecté à cette matière.");
        }

        if (resultatRepo.existsByExamen_Id(examenId)) {
            throw new NotesProfesseurException("Suppression impossible: des résultats existent déjà.");
        }

        examenRepo.delete(examen);
    }
    @Transactional
    @Override
    public ExamenSortieDTO deposerCorrigeExamen(String ppr, Long examenId, MultipartFile pdf) {

        Examen examen = examenRepo.findByIdWithMatiere(examenId)
                .orElseThrow(() -> new NotesProfesseurException("Examen introuvable."));

        // ownership par matière (Option B) - même logique que modifier/supprimer
        boolean affecte = affectationRepo.findActivesParProfesseurAvecDetails(ppr).stream()
                .anyMatch(ae -> ae.getMatiere() != null
                        && examen.getMatiere().getCodeMatiere().equals(ae.getMatiere().getCodeMatiere()));

        if (!affecte) {
            throw new NotesProfesseurException("Accès refusé: vous n'êtes pas affecté à cette matière.");
        }

        // si remplacement : supprimer l'ancien fichier
        String ancien = examen.getCorrigePath();

        String path = stockage.sauvegarderPdf(pdf, "examens/corriges");
        examen.setCorrigePath(path);

        Examen saved = examenRepo.save(examen);

        if (ancien != null && !ancien.isBlank() && !ancien.equals(path)) {
            stockage.supprimerSilencieusement(ancien);
        }

        return toSortieDTO(saved);
    }

    @Transactional
    @Override
    public void supprimerCorrigeExamen(String ppr, Long examenId) {

        Examen examen = examenRepo.findByIdWithMatiere(examenId)
                .orElseThrow(() -> new NotesProfesseurException("Examen introuvable."));

        boolean affecte = affectationRepo.findActivesParProfesseurAvecDetails(ppr).stream()
                .anyMatch(ae -> ae.getMatiere() != null
                        && examen.getMatiere().getCodeMatiere().equals(ae.getMatiere().getCodeMatiere()));

        if (!affecte) {
            throw new NotesProfesseurException("Accès refusé: vous n'êtes pas affecté à cette matière.");
        }

        String ancien = examen.getCorrigePath();
        if (ancien == null || ancien.isBlank()) {
            return;
        }

        examen.setCorrigePath(null);
        examenRepo.save(examen);
        stockage.supprimerSilencieusement(ancien);
    }


    @Transactional(readOnly = true)
    @Override
    public Resource telechargerCorrigeExamenProf(String ppr, Long examenId) {

        Examen examen = examenRepo.findByIdWithMatiere(examenId)
                .orElseThrow(() -> new NotesProfesseurException("Examen introuvable."));

        boolean affecte = affectationRepo.findActivesParProfesseurAvecDetails(ppr).stream()
                .anyMatch(ae -> ae.getMatiere() != null
                        && examen.getMatiere().getCodeMatiere().equals(ae.getMatiere().getCodeMatiere()));

        if (!affecte) {
            throw new NotesProfesseurException("Accès refusé: vous n'êtes pas affecté à cette matière.");
        }

        if (examen.getCorrigePath() == null || examen.getCorrigePath().isBlank()) {
            throw new NotesProfesseurException("Aucun corrigé disponible.");
        }

        return stockage.chargerCommeResource(examen.getCorrigePath());
    }



}

