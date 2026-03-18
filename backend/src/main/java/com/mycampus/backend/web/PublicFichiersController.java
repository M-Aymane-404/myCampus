package com.mycampus.backend.web;

import com.mycampus.backend.exceptions.FilesException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/public/fichiers")
public class PublicFichiersController {

    @Value("${app.files.base-dir:uploads}")
    private String baseDir;

    @GetMapping("/voir")
    public ResponseEntity<Resource> voir(@RequestParam("chemin") String chemin) {
        Path fichier = resoudreCheminSecurise(chemin);
        if (fichier == null) {
            return buildPlaceholderPdfResponse(chemin);
        }

        Resource resource = new FileSystemResource(fichier);

        String contentType = detecterContentType(fichier);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(fichier.getFileName().toString()).build().toString()
                )
                .body(resource);
    }

    private Path resoudreCheminSecurise(String chemin) {
        if (!StringUtils.hasText(chemin)) {
            throw new FilesException("Le chemin du fichier est obligatoire.");
        }

        Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
        String normalized = chemin.trim().replace("\\", "/");
        String normalizedBasePath = basePath.toString().replace("\\", "/");

        String relativePath;
        if (normalized.startsWith(normalizedBasePath + "/")) {
            relativePath = normalized.substring((normalizedBasePath + "/").length());
        } else if (normalized.contains("/uploads/")) {
            relativePath = normalized.substring(normalized.lastIndexOf("/uploads/") + "/uploads/".length());
        } else if (normalized.startsWith("uploads/")) {
            relativePath = normalized.substring("uploads/".length());
        } else {
            relativePath = normalized.replaceFirst("^/+", "");
        }

        if (!StringUtils.hasText(relativePath)) {
            throw new FilesException("Chemin de fichier invalide.");
        }

        Path resolved = basePath.resolve(relativePath).normalize();
        if (!resolved.startsWith(basePath)) {
            throw new FilesException("Acces refuse a ce fichier.");
        }
        if (!Files.exists(resolved) || Files.isDirectory(resolved)) {
            Path legacyResolved = resoudreDepuisAncienDossierUploads(relativePath, basePath);
            if (legacyResolved != null) {
                return legacyResolved;
            }
            if (isLegacySeedDocument(relativePath)) {
                return null;
            }
            throw new FilesException("Fichier introuvable.");
        }

        return resolved;
    }

    private boolean isLegacySeedDocument(String relativePath) {
        String normalized = relativePath.replace("\\", "/").toLowerCase();
        return normalized.startsWith("corriges/")
                || normalized.startsWith("copies/")
                || normalized.startsWith("examens/corriges/")
                || normalized.startsWith("resultats/copies/");
    }

    private Path resoudreDepuisAncienDossierUploads(String relativePath, Path basePath) {
        Path legacyBasePath = Paths.get("uploads").toAbsolutePath().normalize();
        if (legacyBasePath.equals(basePath)) {
            return null;
        }

        Path candidate = legacyBasePath.resolve(relativePath).normalize();
        if (!candidate.startsWith(legacyBasePath)) {
            return null;
        }

        if (!Files.exists(candidate) || Files.isDirectory(candidate)) {
            return null;
        }

        return candidate;
    }

    private String detecterContentType(Path fichier) {
        try {
            String contentType = Files.probeContentType(fichier);
            return StringUtils.hasText(contentType) ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        } catch (IOException ex) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    private ResponseEntity<Resource> buildPlaceholderPdfResponse(String chemin) {
        String fileName = extraireNomFichier(chemin);
        String message = "Document de demonstration indisponible en stockage local : " + fileName;
        byte[] pdfBytes = buildMinimalPdf(message);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(fileName).build().toString()
                )
                .body(new ByteArrayResource(pdfBytes));
    }

    private String extraireNomFichier(String chemin) {
        String normalized = chemin == null ? "document.pdf" : chemin.replace("\\", "/").trim();
        int index = normalized.lastIndexOf('/');
        String fileName = index >= 0 ? normalized.substring(index + 1) : normalized;
        return StringUtils.hasText(fileName) ? fileName : "document.pdf";
    }

    private byte[] buildMinimalPdf(String message) {
        String safeMessage = (message == null ? "Document indisponible" : message)
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
        String content = "BT\n/F1 16 Tf\n72 760 Td\n(" + safeMessage + ") Tj\nET\n";

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<Integer> offsets = new java.util.ArrayList<>();

            writeAscii(out, "%PDF-1.4\n");

            offsets.add(out.size());
            writeAscii(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

            offsets.add(out.size());
            writeAscii(out, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

            offsets.add(out.size());
            writeAscii(out, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n");

            offsets.add(out.size());
            writeAscii(out, "4 0 obj\n<< /Length " + content.getBytes(StandardCharsets.US_ASCII).length + " >>\nstream\n");
            writeAscii(out, content);
            writeAscii(out, "endstream\nendobj\n");

            offsets.add(out.size());
            writeAscii(out, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

            int xrefOffset = out.size();
            writeAscii(out, "xref\n0 6\n");
            writeAscii(out, "0000000000 65535 f \n");
            for (Integer offset : offsets) {
                writeAscii(out, String.format("%010d 00000 n \n", offset));
            }
            writeAscii(out, "trailer\n<< /Size 6 /Root 1 0 R >>\n");
            writeAscii(out, "startxref\n" + xrefOffset + "\n%%EOF");

            return out.toByteArray();
        } catch (IOException ex) {
            return "%PDF-1.4\n%%EOF".getBytes(StandardCharsets.US_ASCII);
        }
    }

    private void writeAscii(ByteArrayOutputStream out, String value) throws IOException {
        out.write(value.getBytes(StandardCharsets.US_ASCII));
    }
}
