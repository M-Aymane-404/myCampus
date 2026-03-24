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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to load and filter professor timetable from JSON files
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmploiTempsProfesseurMetier {

    private final ResourcePatternResolver resourcePatternResolver;
    private static final Pattern ANNEE_PATTERN = Pattern.compile("_(\\d{4})_");

    public String chargerEmploiTempsProfesseur(String professeurPpr) {
        if (professeurPpr == null || professeurPpr.isBlank()) {
            throw new UtilisateursException("PPR du professeur obligatoire");
        }

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        var slotsResult = mapper.createArrayNode();
        int anneeCourante = LocalDate.now().getYear();
        String professeurPprNormalise = normaliserPpr(professeurPpr);

        try {
            // classpath* pour fonctionner aussi une fois packagé (jar)
            String pattern = "classpath*:emplois/classes/*_*_*.json";
            Resource[] resources = resourcePatternResolver.getResources(pattern);

            Map<Integer, List<Resource>> resourcesParAnnee = new HashMap<>();
            for (Resource resource : resources) {
                Integer annee = extraireAnnee(resource.getFilename());
                if (annee == null) {
                    continue;
                }
                resourcesParAnnee.computeIfAbsent(annee, ignored -> new ArrayList<>()).add(resource);
            }

            int anneeCible = anneeCourante;
            if (!resourcesParAnnee.containsKey(anneeCible) && !resourcesParAnnee.isEmpty()) {
                anneeCible = resourcesParAnnee.keySet().stream().mapToInt(Integer::intValue).max().orElse(anneeCourante);
            }

            List<Resource> resourcesCibles = resourcesParAnnee.getOrDefault(anneeCible, List.of());
            if (resourcesCibles.isEmpty()) {
                log.warn("No timetable resources found for year {} (pattern={})", anneeCible, pattern);
            }

            for (Resource resource : resourcesCibles) {
                try (var in = resource.getInputStream()) {
                    JsonNode root = mapper.readTree(in);
                    JsonNode slots = root.get("slots");

                    if (slots != null && slots.isArray()) {
                        for (JsonNode slot : slots) {
                            String slotPpr = normaliserPpr(slot.path("professeurPpr").asText());
                            if (professeurPprNormalise.equals(slotPpr)) {
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

    private static String normaliserPpr(String ppr) {
        return ppr == null ? "" : ppr.trim().toUpperCase();
    }

    private static Integer extraireAnnee(String filename) {
        if (filename == null) {
            return null;
        }
        Matcher matcher = ANNEE_PATTERN.matcher(filename);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            return null;
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
