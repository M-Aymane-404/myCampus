package com.mycampus.backend.metier.etudiant.motifDePresence;

import com.mycampus.backend.dto.FichierStocker.FichierStockerDTO;
import com.mycampus.backend.dto.etudiant.DonneeEntrer.EtudiantJustificationPresenceDTO;
import com.mycampus.backend.entites.Presence;
import com.mycampus.backend.enumerations.StatutJustificatif;
import com.mycampus.backend.enumerations.StatusAbsence;
import com.mycampus.backend.enumerations.TypeMotif;
import com.mycampus.backend.exceptions.PresenceException;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.repositorys.PresenceRepository;
import com.mycampus.backend.metier.security.LienFichierSecuriseService;
import com.mycampus.backend.metier.security.ValidationFichierSecuriseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GestionDeMotifDePresenceMetierImpl implements GestionDeMotifDePresenceMetier {

    private final PresenceRepository presenceRepository;
    private final ValidationFichierSecuriseService validationFichierSecuriseService;
    private final LienFichierSecuriseService lienFichierSecuriseService;

    @Value("${app.files.base-dir:uploads}")
    private String baseDir;

    public FichierStockerDTO enregisterMotifDePresence(MultipartFile file) {
        ValidationFichierSecuriseService.FichierValide fichierValide = validationFichierSecuriseService.valider(
                file,
                java.util.Set.of(
                        ValidationFichierSecuriseService.FormatFichierAutorise.JPEG,
                        ValidationFichierSecuriseService.FormatFichierAutorise.PNG,
                        ValidationFichierSecuriseService.FormatFichierAutorise.PDF
                ),
                5 * 1024 * 1024L
        );

        String filename = UUID.randomUUID() + fichierValide.extension();

        Path dir = Paths.get(baseDir).toAbsolutePath().normalize().resolve("motifsPresence").normalize();
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(filename).normalize();
            if (!target.startsWith(dir)) {
                throw new PresenceException("Chemin du justificatif invalide.");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Erreur stockage fichier", e);
        }

        return new FichierStockerDTO(filename, null);
    }

    public void supprimerMotifEtudiantSiExsist(String motifNom) {
        if (!StringUtils.hasText(motifNom)) return;

        Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
        Path motifDir = basePath.resolve("motifsPresence").normalize();
        Path path = motifDir.resolve(motifNom).normalize();

        if (!path.startsWith(motifDir)) {
            throw new PresenceException("Chemin du justificatif invalide.");
        }

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // on log seulement, pas bloquant
        }
    }

    @Override
    public void ajouterJustification_Absence(Long idEtudiant, MultipartFile motif, Long presenceId, EtudiantJustificationPresenceDTO dto) throws PresenceException {
        if (presenceId == null) {
            throw new UtilisateursException("id de presence est obligatoire");
        }
        if (dto.getTypeMotif() == null) {
            throw new PresenceException("type motif obligatoire");
        }
        if (idEtudiant == null) {
            throw new UtilisateursException("id d'etudiant est obligatoire");
        }
        if (dto.getTypeMotif().equals(TypeMotif.AUTRE)
                && !StringUtils.hasText(dto.getDescriptionDeJustification())) {
            throw new PresenceException("description de justification obligatoire");
        }

        Presence p = presenceRepository.findByIdAndEtudiant_Id(presenceId, idEtudiant)
                .orElseThrow(() -> new PresenceException("etudiant ou presence introuvable"));

        LocalDateTime now = LocalDateTime.now();
        if (p.getDateDeclaration() != null) {
            Duration duration = Duration.between(p.getDateDeclaration(), now);
            if (duration.toHours() >= 48) {
                throw new PresenceException("Delai depasse : vous ne pouvez plus ajouter ou modifier une justification apres 48 heures.");
            }
        }

        if (p.getStatus().equals(StatusAbsence.PRESENT)) {
            throw new PresenceException("vous etre present ");
        }
        if (p.getStatutJustificatif() == StatutJustificatif.VALIDE) {
            throw new PresenceException("motif deja valider");
        }
        if (dto.getTypeMotif() == TypeMotif.CERTIFICAT_MEDICAL && (motif == null || motif.isEmpty())) {
            throw new PresenceException("motif obligatoire");
        }
        if (dto.getTypeMotif().equals(TypeMotif.RAISON_FAMILIALE) && motif == null) {
            throw new PresenceException("motif obligatoire");
        }

        supprimerMotifEtudiantSiExsist(p.getMotifNom());

        FichierStockerDTO stored = null;
        if (motif != null && !motif.isEmpty()) {
            stored = enregisterMotifDePresence(motif);
            p.setMotifNom(stored.fileName());
            p.setMotifUrl(lienFichierSecuriseService.construireUrlJustificatif(p.getId(), stored.fileName()));
        } else {
            p.setMotifNom(null);
            p.setMotifUrl(null);
        }

        p.setDescriptionDeJustification(dto.getDescriptionDeJustification());
        p.setTypeMotif(dto.getTypeMotif());
        p.setStatutJustificatif(StatutJustificatif.EN_ATTENTE);

        presenceRepository.save(p);
    }

    @Override
    public Resource chargerMotifDePresence(String motifNom) {
        if (!StringUtils.hasText(motifNom)) {
            throw new PresenceException("Nom du justificatif absent ou invalide.");
        }

        Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
        Path motifDir = basePath.resolve("motifsPresence").normalize();
        Path motifPath = motifDir.resolve(motifNom).normalize();

        if (!motifPath.startsWith(motifDir)) {
            throw new PresenceException("Chemin du justificatif invalide.");
        }

        if (!Files.exists(motifPath) || Files.isDirectory(motifPath)) {
            throw new PresenceException("Justificatif introuvable.");
        }

        try {
            return new UrlResource(motifPath.toUri());
        } catch (MalformedURLException e) {
            throw new PresenceException("Impossible de charger le justificatif.", e);
        }
    }
}
