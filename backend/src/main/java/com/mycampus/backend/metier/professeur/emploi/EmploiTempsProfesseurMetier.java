package com.mycampus.backend.metier.professeur.emploi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycampus.backend.enumerations.Semestre;
import com.mycampus.backend.exceptions.UtilisateursException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to load and filter professor timetable from JSON files
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmploiTempsProfesseurMetier {

    private final ResourcePatternResolver resourcePatternResolver;

    public String chargerEmploiTempsProfesseur(String professeurPpr) {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        var slotsResult = mapper.createArrayNode();
        LocalDate date = LocalDate.now();

        try {
            String pattern = "classpath:emplois/classes/*_"+date.getYear()+"_*.json";
            Resource[] resources = resourcePatternResolver.getResources(pattern);

            for (Resource resource : resources) {
                try (var in = resource.getInputStream()) {
                    JsonNode root = mapper.readTree(in);
                    JsonNode slots = root.get("slots");

                    if (slots != null && slots.isArray()) {
                        for (JsonNode slot : slots) {
                            if (professeurPpr.equals(slot.path("professeurPpr").asText())) {
                                slotsResult.add(slot);
                            }
                        }
                    }
                } catch (IOException e) {
                    log.warn("Error reading timetable file: {}", resource.getFilename(), e);
                }
            }

            var response = mapper.createObjectNode();
            response.put("professeurPpr", professeurPpr);
            response.set("slots", slotsResult);

            return mapper.writeValueAsString(response);

        } catch (IOException e) {
            throw new UtilisateursException("Erreur lors du chargement des emplois du temps: " + e.getMessage());
        }
    }


    public JsonNode getProchaineSeance(String ppr) {

        String jsonString = chargerEmploiTempsProfesseur(ppr);

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