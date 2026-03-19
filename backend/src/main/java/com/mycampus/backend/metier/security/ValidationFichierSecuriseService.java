package com.mycampus.backend.metier.security;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Set;

@Service
public class ValidationFichierSecuriseService {

    public enum FormatFichierAutorise {
        PDF(".pdf", "application/pdf"),
        JPEG(".jpg", "image/jpeg"),
        PNG(".png", "image/png");

        private final String extension;
        private final String typeMime;

        FormatFichierAutorise(String extension, String typeMime) {
            this.extension = extension;
            this.typeMime = typeMime;
        }

        public String extension() {
            return extension;
        }

        public String typeMime() {
            return typeMime;
        }
    }

    public record FichierValide(String nomOriginalSecurise, String extension, String typeMime) {
    }

    public FichierValide valider(MultipartFile fichier, Set<FormatFichierAutorise> formatsAutorises, long tailleMaxOctets) {
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est obligatoire.");
        }
        if (fichier.getSize() > tailleMaxOctets) {
            throw new IllegalArgumentException("Le fichier depasse la taille autorisee.");
        }

        FormatFichierAutorise formatDetecte = detecterFormat(fichier);
        if (!formatsAutorises.contains(formatDetecte)) {
            throw new IllegalArgumentException("Type de fichier non autorise.");
        }

        String nomOriginal = StringUtils.hasText(fichier.getOriginalFilename())
                ? fichier.getOriginalFilename()
                : "fichier" + formatDetecte.extension();
        String nomNettoye = Paths.get(nomOriginal).getFileName().toString().replace("\\", "/");
        int dernierPoint = nomNettoye.lastIndexOf('.');
        String baseNom = dernierPoint > 0 ? nomNettoye.substring(0, dernierPoint) : nomNettoye;
        baseNom = baseNom.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (!StringUtils.hasText(baseNom)) {
            baseNom = "fichier";
        }

        return new FichierValide(baseNom + formatDetecte.extension(), formatDetecte.extension(), formatDetecte.typeMime());
    }

    private FormatFichierAutorise detecterFormat(MultipartFile fichier) {
        try (InputStream inputStream = fichier.getInputStream()) {
            byte[] entete = inputStream.readNBytes(8);
            if (commencePar(entete, new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D})) {
                return FormatFichierAutorise.PDF;
            }
            if (commencePar(entete, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A})) {
                return FormatFichierAutorise.PNG;
            }
            if (commencePar(entete, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})) {
                return FormatFichierAutorise.JPEG;
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Impossible de lire le fichier.", ex);
        }
        throw new IllegalArgumentException("Signature binaire du fichier non reconnue.");
    }

    private boolean commencePar(byte[] source, byte[] prefixe) {
        if (source.length < prefixe.length) {
            return false;
        }
        for (int i = 0; i < prefixe.length; i++) {
            if (source[i] != prefixe[i]) {
                return false;
            }
        }
        return true;
    }
}
