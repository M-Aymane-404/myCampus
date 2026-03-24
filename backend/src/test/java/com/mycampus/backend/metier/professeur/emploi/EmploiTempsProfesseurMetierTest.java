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

        String json = metier.chargerEmploiTempsProfesseur("PPR200012");

        JsonNode root = new ObjectMapper().readTree(json);
        assertThat(root.path("professeurPpr").asText()).isEqualTo("PPR200012");

        JsonNode slots = root.path("slots");
        assertThat(slots.isArray()).isTrue();
        assertThat(slots.size()).isGreaterThan(0);

        for (JsonNode slot : slots) {
            assertThat(slot.path("professeurPpr").asText()).isEqualToIgnoringCase("PPR200012");
        }
    }
}

