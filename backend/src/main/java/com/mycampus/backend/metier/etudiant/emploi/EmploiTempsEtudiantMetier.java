package com.mycampus.backend.metier.etudiant.emploi;

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




    public JsonNode getProchaineSeance(String codeClasse, Semestre semestre) {

        String jsonString = chargerEmploiDuTempsEtudiant(codeClasse, semestre);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode emploi = mapper.readTree(jsonString);
            JsonNode slots = emploi.path("slots");

            if (!slots.isArray() || slots.isEmpty()) {
                return null;
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDate today = LocalDate.now();

            JsonNode nextSlot = null;
            LocalDateTime nextDateTime = null;

            for (JsonNode slot : slots) {

                DayOfWeek day = parseDayOfWeek(slot.get("dayOfWeek").asText());
                LocalTime start = LocalTime.parse(
                        slot.get("start").asText()
                );

                LocalDate nextDate = today.with(TemporalAdjusters.nextOrSame(day));
                LocalDateTime slotDateTime = LocalDateTime.of(nextDate, start);

                // si déjà passé aujourd’hui → semaine suivante
                if (!slotDateTime.isAfter(now)) {
                    slotDateTime = slotDateTime.plusWeeks(1);
                }

                if (nextDateTime == null || slotDateTime.isBefore(nextDateTime)) {
                    nextDateTime = slotDateTime;
                    nextSlot = slot;
                }
            }

            return nextSlot;

        } catch (Exception e) {
            throw new UtilisateursException("Erreur calcul prochaine séance");
        }
    }


    /**
     * Supporte:
     * - EN: MONDAY, Tuesday, monday...
     * - FR: LUNDI, Mardi, mercredi...
     */
    private DayOfWeek parseDayOfWeek(String input) {
        String s = input.trim().toUpperCase();

        // EN (MONDAY...) et variantes
        try {
            return DayOfWeek.valueOf(s);
        } catch (Exception ignored) {
            // FR -> EN
            return switch (s) {
                case "LUNDI" -> DayOfWeek.MONDAY;
                case "MARDI" -> DayOfWeek.TUESDAY;
                case "MERCREDI" -> DayOfWeek.WEDNESDAY;
                case "JEUDI" -> DayOfWeek.THURSDAY;
                case "VENDREDI" -> DayOfWeek.FRIDAY;
                case "SAMEDI" -> DayOfWeek.SATURDAY;
                case "DIMANCHE" -> DayOfWeek.SUNDAY;
                default -> throw new IllegalArgumentException("Jour invalide: " + input);
            };
        }
    }


}
