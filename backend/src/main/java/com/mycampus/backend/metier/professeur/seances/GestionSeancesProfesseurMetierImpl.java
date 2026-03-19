package com.mycampus.backend.metier.professeur.seances;


import com.mycampus.backend.dto.administrateur.DonneSortie.AffectationEnseignementSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.fichiersSeance.DepotFichierSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.CreationSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.ModificationSeanceEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeEntrer.seances.RechercheSeancesEntrerDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.fichiersSeance.FichierSeanceSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.fichiersSeance.TelechargementFichierSeanceSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.seances.SeanceResumeSortieDTO;
import com.mycampus.backend.dto.professeur.DonneeSortie.seances.SeanceSortieDTO;
import com.mycampus.backend.entites.AffectationEnseignement;
import com.mycampus.backend.entites.FichierSeance;
import com.mycampus.backend.entites.Seance;
import com.mycampus.backend.exceptions.SeancesProfesseurException;
import com.mycampus.backend.mappers.administrateur.StructureMapper;
import com.mycampus.backend.mappers.professeur.FichiersSeanceProfesseurMapper;
import com.mycampus.backend.mappers.professeur.SeancesProfesseurMapper;
import com.mycampus.backend.repositorys.AffectationEnseignementRepository;
import com.mycampus.backend.repositorys.FichierSeanceRepository;
import com.mycampus.backend.repositorys.SeanceRepository;
import com.mycampus.backend.metier.professeur.StockageFichiersService;
import com.mycampus.backend.metier.security.ValidationFichierSecuriseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/*
 This service manages professor seances and related files.
 It applies "PPR scoping" to ensure a professor can only access his own data.
*/
@Service
@Transactional
@RequiredArgsConstructor
public class GestionSeancesProfesseurMetierImpl implements GestionSeancesProfesseurMetier {


    private final SeanceRepository seanceRepository;
    private final AffectationEnseignementRepository affectationEnseignementRepository;
    private final FichierSeanceRepository fichierSeanceRepository;

    /* Mappers convert entities to DTOs to avoid exposing JPA entities. */
    private final SeancesProfesseurMapper seancesProfesseurMapper;
    private final FichiersSeanceProfesseurMapper fichiersSeanceProfesseurMapper;
    private final StructureMapper structureMapper;
    private final ValidationFichierSecuriseService validationFichierSecuriseService;
    private final StockageFichiersService stockageFichiersService;
    /*
     This function checks that the professor PPR is provided.
     It prevents empty or null PPR from reaching database queries.
    */
    private void requireProfesseurPpr(String professeurPpr) throws SeancesProfesseurException {
        if (!StringUtils.hasText(professeurPpr)) {
            throw new SeancesProfesseurException("Le PPR du professeur est obligatoire.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AffectationEnseignementSortieDTO> listerEnseignements(String professeurPpr)
            throws SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        // StructureMapper peut accéder à .getNom(), .getCodeClasse() etc. sans LazyException
        List<AffectationEnseignement> affectations =
                affectationEnseignementRepository.findActivesParProfesseurAvecDetails(professeurPpr);

        return affectations.stream()
                .map(structureMapper::FromAffectationEnseignementVersAffectationEnseignementSortieDTO)
                .collect(Collectors.toList());
    }

    private AffectationEnseignement findActiveAffectationByProfAndCodes(
            String professeurPpr,
            String codeClasse,
            String codeMatiere,
             String anneeUniversitaire
    ) throws SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        if (!StringUtils.hasText(codeClasse)) {
            throw new SeancesProfesseurException("Code classe obligatoire.");
        }
        if (!StringUtils.hasText(codeMatiere)) {
            throw new SeancesProfesseurException("Code matière obligatoire.");
        }
        if (!StringUtils.hasText(anneeUniversitaire)) {
            throw new SeancesProfesseurException("Année universitaire obligatoire.");
        }

        return affectationEnseignementRepository
                .findByActiveTrueAndProfesseur_PprAndClasse_CodeClasseAndMatiere_CodeMatiereAndAnneeUniversitaire(
                        professeurPpr, codeClasse, codeMatiere,anneeUniversitaire
                )
                .orElseThrow(() -> new SeancesProfesseurException(
                        "Affectation active introuvable ou non autorisée pour ce professeur (classe/matière)."
                ));
    }

    //ownership seance prof

    private Seance requireSeancePourProfesseur(String professeurPpr, Long seanceId) throws SeancesProfesseurException {
        requireProfesseurPpr(professeurPpr);

        if (seanceId == null) {
            throw new SeancesProfesseurException("L'identifiant de la séance est obligatoire.");
        }

        return seanceRepository
                .findByIdAndPprAvecDetails(seanceId, professeurPpr)
                .orElseThrow(() -> new SeancesProfesseurException("Séance introuvable ou non autorisée."));
    }


    //FichierSeance belongs to PPR.

    private FichierSeance requireFichierSeancePourProfesseur(String professeurPpr, Long fichierSeanceId)
            throws SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        if (fichierSeanceId == null) {
            throw new SeancesProfesseurException("L'identifiant du fichier est obligatoire.");
        }

        return fichierSeanceRepository
                .findByIdAndSeance_AffectationEnseignement_Professeur_Ppr(fichierSeanceId, professeurPpr)
                .orElseThrow(() -> new SeancesProfesseurException("Fichier introuvable ou non autorisé."));
    }

 //validation heure
    private void validerCoherenceHoraire(LocalDateTime heureDebut, LocalDateTime heureFin)
            throws SeancesProfesseurException {
        if (heureDebut != null && heureFin != null && !heureDebut.isBefore(heureFin)) {
            throw new SeancesProfesseurException("L'heure de début doit être strictement avant l'heure de fin.");
        }
    }

    /*
     This function stores an uploaded file in a local folder and returns the saved path.
     Security: prevents writing outside the target folder.
    */
    private StockageFichiersService.FichierStocke sauvegarderFichierSurDisque(MultipartFile fichier) throws SeancesProfesseurException {
        try {
            return stockageFichiersService.sauvegarderFichier(
                    fichier,
                    "seances",
                    java.util.Set.of(
                            ValidationFichierSecuriseService.FormatFichierAutorise.PDF,
                            ValidationFichierSecuriseService.FormatFichierAutorise.JPEG,
                            ValidationFichierSecuriseService.FormatFichierAutorise.PNG
                    ),
                    10 * 1024 * 1024L
            );
        } catch (SeancesProfesseurException e) {
            throw e;
        } catch (Exception e) {
            throw new SeancesProfesseurException("Erreur lors du stockage du fichier: " + e.getMessage());
        }
    }


    /*
     This function deletes a file path from disk safely (no crash if file is missing).
    */
    private void supprimerFichierSurDisqueSilencieusement(String chemin) {
        if (!StringUtils.hasText(chemin)) {
            return;
        }
        stockageFichiersService.supprimerSilencieusement(chemin);
    }

    @Override
    public SeanceSortieDTO creerSeance(String professeurPpr, CreationSeanceEntrerDTO creationSeanceEntrerDTO)
            throws SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        if (creationSeanceEntrerDTO == null) {
            throw new SeancesProfesseurException("Les données de création de séance sont obligatoires.");
        }
        validerCoherenceHoraire(creationSeanceEntrerDTO.getHeureDebut(),creationSeanceEntrerDTO.getHeureFin());

        /*
         The affectation is required and must belong to the professor.
         This is the main ownership check for creating seances.
        */
        AffectationEnseignement affectation = findActiveAffectationByProfAndCodes(
                professeurPpr,
                creationSeanceEntrerDTO.getCodeClasse(),
                creationSeanceEntrerDTO.getCodeMatiere(),
                creationSeanceEntrerDTO.getAnneeUniversitaire()

        );


        Seance seance = new Seance();

        seance.setType(creationSeanceEntrerDTO.getType());
        seance.setDate(creationSeanceEntrerDTO.getDate());
        seance.setHeureDebut(creationSeanceEntrerDTO.getHeureDebut());
        seance.setHeureFin(creationSeanceEntrerDTO.getHeureFin());
        seance.setSalle(creationSeanceEntrerDTO.getSalle());
        seance.setEtat(creationSeanceEntrerDTO.getEtat());
        seance.setContenu(creationSeanceEntrerDTO.getContenu());

        seance.setAffectationEnseignement(affectation);

        Seance seanceSauvegardee = seanceRepository.save(seance);

        return seancesProfesseurMapper.FromSeanceVersSeanceSortieDTO(seanceSauvegardee);
    }

    @Override
    public SeanceSortieDTO modifierSeance(String professeurPpr, Long seanceId, ModificationSeanceEntrerDTO modificationSeanceEntrerDTO)
            throws SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        if (modificationSeanceEntrerDTO == null) {
            throw new SeancesProfesseurException("Les données de modification de séance sont obligatoires.");
        }


        /*
         Load seance only if it belongs to the professor.
        */
        Seance seance = requireSeancePourProfesseur(professeurPpr, seanceId);

        java.time.LocalDateTime heureDebutFinale = modificationSeanceEntrerDTO.getHeureDebut() != null
                ? modificationSeanceEntrerDTO.getHeureDebut()
                : seance.getHeureDebut();

        java.time.LocalDateTime heureFinFinale = modificationSeanceEntrerDTO.getHeureFin() != null
                ? modificationSeanceEntrerDTO.getHeureFin()
                : seance.getHeureFin();

        validerCoherenceHoraire(heureDebutFinale, heureFinFinale);



        /*
         Optional update strategy:
         We update only non-null fields, so partial updates are possible.
        */
        if (modificationSeanceEntrerDTO.getType() != null) {
            seance.setType(modificationSeanceEntrerDTO.getType());
        }
        if (modificationSeanceEntrerDTO.getDate() != null) {
            seance.setDate(modificationSeanceEntrerDTO.getDate());
        }
        if (modificationSeanceEntrerDTO.getHeureDebut() != null) {
            seance.setHeureDebut(modificationSeanceEntrerDTO.getHeureDebut());
        }
        if (modificationSeanceEntrerDTO.getHeureFin() != null) {
            seance.setHeureFin(modificationSeanceEntrerDTO.getHeureFin());
        }
        if (modificationSeanceEntrerDTO.getSalle() != null) {
            seance.setSalle(modificationSeanceEntrerDTO.getSalle());
        }
        if (modificationSeanceEntrerDTO.getEtat() != null) {
            seance.setEtat(modificationSeanceEntrerDTO.getEtat());
        }
        if (modificationSeanceEntrerDTO.getContenu() != null) {
            seance.setContenu(modificationSeanceEntrerDTO.getContenu());
        }


        Seance seanceModifiee = seanceRepository.save(seance);

        return seancesProfesseurMapper.FromSeanceVersSeanceSortieDTO(seanceModifiee);
    }

    @Override
    public void supprimerSeance(String professeurPpr, Long seanceId) throws SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        /*
         Load seance only if it belongs to the professor.
        */
        Seance seance = requireSeancePourProfesseur(professeurPpr, seanceId);

        List<FichierSeance> fichiers = fichierSeanceRepository.findBySeance_Id(seance.getId());
        for (FichierSeance fichier : fichiers) {
            supprimerFichierSurDisqueSilencieusement(fichier.getChemin());
        }
        fichierSeanceRepository.deleteBySeanceId(seance.getId());


        seanceRepository.delete(seance);
    }

    @Override
    @Transactional(readOnly = true)
    public SeanceSortieDTO trouverSeanceParId(String professeurPpr, Long seanceId) throws SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        
        Seance seance = requireSeancePourProfesseur(professeurPpr, seanceId);

        return seancesProfesseurMapper.FromSeanceVersSeanceSortieDTO(seance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeanceResumeSortieDTO> rechercherSeances(String professeurPpr, RechercheSeancesEntrerDTO rechercheSeancesEntrerDTO)
            throws SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        if (rechercheSeancesEntrerDTO == null) {
            throw new SeancesProfesseurException("Les critères de recherche sont obligatoires.");
        }

        /*
         First step: load all seances of this professor (PPR scope).
         This is safe and easy to maintain.
        */
        List<Seance> seances = seanceRepository.findByPprAvecDetails(professeurPpr);

        /*
         Second step: apply filters in memory.
         This avoids building complex dynamic JPQL queries.
        */
        return seances.stream()
                .filter(s -> rechercheSeancesEntrerDTO.getCodeClasse() == null
                        || (s.getAffectationEnseignement() != null
                        && s.getAffectationEnseignement().getClasse() != null
                        && rechercheSeancesEntrerDTO.getCodeClasse().equals(s.getAffectationEnseignement().getClasse().getCodeClasse())))
                .filter(s -> rechercheSeancesEntrerDTO.getCodeMatiere() == null
                        || (s.getAffectationEnseignement() != null
                        && s.getAffectationEnseignement().getMatiere() != null
                        && rechercheSeancesEntrerDTO.getCodeMatiere().equals(s.getAffectationEnseignement().getMatiere().getCodeMatiere())))
                .filter(s -> rechercheSeancesEntrerDTO.getAnneeUniversitaire() == null
                        || (s.getAffectationEnseignement() != null
                        && rechercheSeancesEntrerDTO.getAnneeUniversitaire().equals(s.getAffectationEnseignement().getAnneeUniversitaire())))


                .filter(s -> rechercheSeancesEntrerDTO.getType() == null || rechercheSeancesEntrerDTO.getType().equals(s.getType()))
                .filter(s -> rechercheSeancesEntrerDTO.getEtat() == null || rechercheSeancesEntrerDTO.getEtat().equals(s.getEtat()))
                .filter(s -> rechercheSeancesEntrerDTO.getDateDebut() == null || (s.getDate() != null && !s.getDate().isBefore(rechercheSeancesEntrerDTO.getDateDebut())))
                .filter(s -> rechercheSeancesEntrerDTO.getDateFin() == null || (s.getDate() != null && !s.getDate().isAfter(rechercheSeancesEntrerDTO.getDateFin())))
                .map(seancesProfesseurMapper::FromSeanceVersSeanceResumeSortieDTO)
                .collect(Collectors.toList());

    }

    @Override
    public FichierSeanceSortieDTO deposerFichierPourSeance(String professeurPpr, DepotFichierSeanceEntrerDTO depotFichierSeanceEntrerDTO, MultipartFile fichier)
            throws SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        if (depotFichierSeanceEntrerDTO == null) {
            throw new SeancesProfesseurException("Les données de dépôt du fichier sont obligatoires.");
        }

        /*
         The file is always linked to one Seance.
         Ownership check is done by loading the seance with professor PPR scope.
        */
        Seance seance = requireSeancePourProfesseur(professeurPpr, depotFichierSeanceEntrerDTO.getSeanceId());

        StockageFichiersService.FichierStocke fichierStocke = sauvegarderFichierSurDisque(fichier);
        FichierSeance fichierSeance = new FichierSeance();

        fichierSeance.setSeance(seance);
        fichierSeance.setNomOriginal(fichierStocke.nomOriginal());
        fichierSeance.setChemin(fichierStocke.cheminRelatif());
        fichierSeance.setTypeMime(fichierStocke.typeMime());
        fichierSeance.setTaille(fichierStocke.taille());
        fichierSeance.setDateUpload(LocalDateTime.now());

        FichierSeance fichierSauvegarde = fichierSeanceRepository.save(fichierSeance);

        return fichiersSeanceProfesseurMapper.FromFichierSeanceVersFichierSeanceSortieDTO(fichierSauvegarde);
    }

    @Override
    public void supprimerFichierDeSeance(String professeurPpr, Long fichierSeanceId) throws SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        /*
         Ownership-safe load:
         the professor can delete only his own files.
        */
        FichierSeance fichierSeance = requireFichierSeancePourProfesseur(professeurPpr, fichierSeanceId);

        supprimerFichierSurDisqueSilencieusement(fichierSeance.getChemin());

        fichierSeanceRepository.delete(fichierSeance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FichierSeanceSortieDTO> listerFichiersDeSeance(String professeurPpr, Long seanceId) throws SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        /*
         Ownership-safe check:
         ensure the seance belongs to the professor before listing its files.
        */
        requireSeancePourProfesseur(professeurPpr, seanceId);

        List<FichierSeance> fichiers = fichierSeanceRepository
                .findBySeance_IdAndSeance_AffectationEnseignement_Professeur_Ppr(seanceId, professeurPpr);

        return fichiersSeanceProfesseurMapper.FromListeFichierSeanceVersListeFichierSeanceSortieDTO(fichiers);
    }


    @Override
    public TelechargementFichierSeanceSortieDTO preparerTelechargementFichierSeance(
            String professeurPpr,
            Long fichierSeanceId
    ) throws SeancesProfesseurException {

        requireProfesseurPpr(professeurPpr);

        FichierSeance fichier = requireFichierSeancePourProfesseur(professeurPpr, fichierSeanceId);

        String chemin = fichier.getChemin();
        if (chemin == null || chemin.isBlank()) {
            throw new SeancesProfesseurException("Chemin du fichier introuvable.");
        }

        try {
            stockageFichiersService.chargerCommeResource(chemin);

            return TelechargementFichierSeanceSortieDTO.builder()
                    .id(fichier.getId())
                    .nomOriginal(fichier.getNomOriginal())
                    .typeMime(fichier.getTypeMime())
                    .taille(fichier.getTaille())
                    .chemin(chemin)
                    .build();

        } catch (Exception e) {
            throw new SeancesProfesseurException("Chemin du fichier invalide.");
        }
    }















}
