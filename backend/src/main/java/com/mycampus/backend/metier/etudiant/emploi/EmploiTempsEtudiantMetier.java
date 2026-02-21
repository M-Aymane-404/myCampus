package com.mycampus.backend.metier.etudiant.emploi;

import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.exceptions.UtilisateursException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmploiTempsEtudiantMetier {

     /**
     * Load timetable JSON from classpath resources
     * @param codeClasse class code (e.g., ILCS1-A)
     * @param semestre semester (e.g., S1)
     * @return JSON content with slots array
     */
    public String chargerEmploiDuTempsEtudiant(String codeClasse, Semestre semestre) {
        String fileName = "emplois/classes/" + codeClasse + "_" + semestre + ".json";
        try {
            ClassPathResource resource = new ClassPathResource(fileName);

            if (!resource.exists()) {
                return """
                {"semestre":"%s","slots":[]}
            """.formatted(semestre);
            }

            try (InputStream in = resource.getInputStream()) {
                return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new UtilisateursException("Erreur lecture emploi du temps");
        }
    }
}
