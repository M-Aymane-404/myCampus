package com.mycampus.backend.metier.etudiant.motifDePresence;

import com.mycampus.backend.dto.FichierStocker.FichierStockerDTO;
import com.mycampus.backend.dto.etudiant.DonneeEntrer.EtudiantJustificationPresenceDTO;
import com.mycampus.backend.entites.Presence;
import com.mycampus.backend.enumerations.StatusAbsence;
import com.mycampus.backend.enumerations.TypeMotif;
import com.mycampus.backend.exceptions.PresenceException;
import com.mycampus.backend.exceptions.UtilisateursException;
import com.mycampus.backend.repositorys.EtudiantRepository;
import com.mycampus.backend.repositorys.PresenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class GestionDeMotifDePresenceMetierImpl implements GestionDeMotifDePresenceMetier {


    private final EtudiantRepository etudiantRepository;
    private final PresenceRepository presenceRepository;
    private static final Set<String> ALLOWED = Set.of(
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/webp",
            "image/heic",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    @Value("${app.files.base-dir:uploads}")
    private String baseDir;

     public FichierStockerDTO enregisterMotifDePresence(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichier justificatif obligatoire");
        }
        if (!ALLOWED.contains(file.getContentType())) {
            throw new IllegalArgumentException("Format non supporté (png, jpg, webp, heic, pdf, doc, docx)");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("La Fichier ne doit pas dépasser 5MB");
        }

        String ext = extensionFromContentType(file.getContentType());
        String filename = UUID.randomUUID() + ext;

        Path dir = Paths.get(baseDir, "motifsPresence");
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Erreur stockage fichier", e);
        }

        String url = "/files/motifsPresence/" + filename; // URL publique
        return new FichierStockerDTO(filename, url);
    }

     public void supprimerMotifEtudiantSiExsist(String motifNom) {
        if (!StringUtils.hasText(motifNom)) return;

        Path path = Paths.get(baseDir, "motifsPresence", motifNom);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // on log seulement, pas bloquant
        }
    }




     @Override
     public void ajouterJustification_Absence(Long idEtudiant, MultipartFile motif, Long presenceId, EtudiantJustificationPresenceDTO dto) throws PresenceException {



         if (presenceId == null  ) {
             throw new UtilisateursException("id de presence est obligatoire");
         }
         if(dto.getTypeMotif() == null){
             throw new PresenceException("type motif obligatoire");
         }

         if(idEtudiant == null){
             throw new UtilisateursException("id d'étudiant est obligatoire");
         }

         if (dto.getTypeMotif().equals(TypeMotif.AUTRE)
                 && !StringUtils.hasText(dto.getDescriptionDeJustification())) {
             throw new PresenceException("description de justification obligatoire");
         }



         Presence p = presenceRepository.findByIdAndEtudiant_Id(presenceId,idEtudiant)
                 .orElseThrow(() -> new PresenceException("étudiant ou presence introuvable"));

         if(p.getStatus().equals(StatusAbsence.PRESENT)){
             throw new PresenceException("vous être present ");
         }

         if(p.isValidationMotif()){
             throw new PresenceException("motif deja valider");
         }
         if (dto.getTypeMotif() == TypeMotif.CERTIFICAT_MEDICAL && (motif == null || motif.isEmpty())){
             throw new PresenceException("motif obligatoire");
         }
         if(dto.getTypeMotif().equals(TypeMotif.RAISON_FAMILIALE) && motif == null){
             throw new PresenceException("motif obligatoire");
         }








        supprimerMotifEtudiantSiExsist(p.getMotifNom());





         FichierStockerDTO stored = null;
         if (motif != null && !motif.isEmpty()) {
             stored = enregisterMotifDePresence(motif);
             p.setMotifNom(stored.fileName());
             p.setMotifUrl(stored.url());
         } else {
             p.setMotifNom(null);
             p.setMotifUrl(null);
         }



        p.setDescriptionDeJustification(dto.getDescriptionDeJustification());
        p.setTypeMotif(dto.getTypeMotif());



         presenceRepository.save(p);

    }



    private String extensionFromContentType(String contentType) {
        return switch (contentType) {

            // Images
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/jpg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/heic" -> ".heic";

            // PDF
            case "application/pdf" -> ".pdf";

            // Word
            case "application/msword" -> ".doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx";

            default -> "";
        };
    }


}
