package com.mycampus.backend.metier.administrateur.emploi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.exceptions.UtilisateursException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmploiTempsMetier {


    public String chargerEmploiDuTemps(String codeClasse, Semestre semestre) {

        LocalDate date = LocalDate.now();
        String fileName = "emplois/classes/" + codeClasse + "_" + date.getYear()+ "_" + semestre + ".json";
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
