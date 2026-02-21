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

    public String chargerEmploiTempsProfesseur(String professeurPpr, Semestre semestre) {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        var slotsResult = mapper.createArrayNode();

        try {
            String pattern = "classpath:emplois/classes/*_" + semestre.name() + ".json";
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
            response.put("semestre", semestre.name());
            response.set("slots", slotsResult);

            return mapper.writeValueAsString(response);

        } catch (IOException e) {
            throw new UtilisateursException("Erreur lors du chargement des emplois du temps: " + e.getMessage());
        }
    }
 }