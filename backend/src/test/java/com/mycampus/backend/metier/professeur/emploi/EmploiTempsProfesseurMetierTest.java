package com.mycampus.backend.metier.professeur.emploi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static org.assertj.core.api.Assertions.assertThat;

class EmploiTempsProfesseurMetierTest {

    @Test
    void chargerEmploiTempsProfesseur_retourneDesSlotsPourUnProfesseurConnu() throws Exception {
        EmploiTempsProfesseurMetier metier =
                new EmploiTempsProfesseurMetier(new PathMatchingResourcePatternResolver());

        String json = metier.chargerEmploiTempsProfesseur("20001200");

        JsonNode root = new ObjectMapper().readTree(json);
        assertThat(root.path("professeurPpr").asText()).isEqualTo("20001200");

        JsonNode slots = root.path("slots");
        assertThat(slots.isArray()).isTrue();
        assertThat(slots.size()).isGreaterThan(0);

        for (JsonNode slot : slots) {
            assertThat(slot.path("professeurPpr").asText()).isEqualToIgnoringCase("20001200");
        }
    }
}
