package com.mycampus.backend.integration;

import com.mycampus.backend.dto.securite.entree.ConnectionMobileDTO;
import com.mycampus.backend.dto.securite.sortie.JetonsSortieDTO;
import com.mycampus.backend.web.Securite.SecurityController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SecurityAuthenticationIntegrationTest {

    @Autowired
    private SecurityController securityController;

    @Test
    void connecterMobile_IdentifiantsValides_RetourneJwtEtRefreshToken() {
        ConnectionMobileDTO payload = ConnectionMobileDTO.builder()
                .email("prof.info1@mycampus.ma")
                .motDePasse("password123")
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "Integration-Test");

        MockHttpServletResponse response = new MockHttpServletResponse();

        JetonsSortieDTO resultat = securityController.connecterMobile(payload, request, response);

        assertNotNull(resultat);
        assertNotNull(resultat.getAccess_token());
        assertTrue(response.getHeader("Set-Cookie").contains("refresh_token="));
    }

    @Test
    void connecterMobile_MotDePasseIncorrect_RejecteAuthentification() {
        ConnectionMobileDTO payload = ConnectionMobileDTO.builder()
                .email("prof.info1@mycampus.ma")
                .motDePasse("mot-de-passe-faux")
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "Integration-Test");

        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThrows(
                BadCredentialsException.class,
                () -> securityController.connecterMobile(payload, request, response)
        );
    }
}
