package com.mycampus.backend.web.Securite;

import com.mycampus.backend.dto.securite.entree.ConnectionDTO;
import com.mycampus.backend.dto.securite.sortie.JetonsSortieDTO;
import com.mycampus.backend.dto.securite.sortie.RotationResult;
import com.mycampus.backend.entites.RefreshToken;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.exceptions.SecuriteException;
import com.mycampus.backend.metier.security.LimitationTentativesConnexionService;
import com.mycampus.backend.metier.security.RefreshTokenMetier;
import com.mycampus.backend.metier.security.RecaptchaService;
import com.mycampus.backend.metier.security.UserDetailsServiceImpl;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private RefreshTokenMetier refreshTokenMetier;
    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private RecaptchaService recaptchaService;
    @Mock
    private LimitationTentativesConnexionService limitationTentativesConnexionService;

    @InjectMocks
    private SecurityController controller;

    private Utilisateur utilisateur;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "cookieSecure", false);
        ReflectionTestUtils.setField(controller, "accessTokenTtlMinutes", 15L);

        utilisateur = Utilisateur.builder()
                .id(1L)
                .nom("Prof")
                .prenom("One")
                .email("prof@mycampus.ma")
                .password("encoded")
                .cin("AA1")
                .active(true)
                .build();

        jwt = Jwt.withTokenValue("access-token")
                .header("alg", "HS512")
                .claim("scope", "ROLE_PROFESSEUR")
                .build();
    }

    @Test
    void connecter_IdentifiantsValides_RetourneTokenEtCreeCookie() {
        ConnectionDTO dto = ConnectionDTO.builder()
                .email("prof@mycampus.ma")
                .motDePasse("secret")
                .recaptchaToken("captcha-ok")
                .build();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "prof@mycampus.ma",
                "secret",
                List.of(new SimpleGrantedAuthority("ROLE_PROFESSEUR"))
        );
        RefreshToken refreshToken = RefreshToken.builder().token("refresh-token").build();

        when(recaptchaService.validateRecaptcha("captcha-ok")).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(utilisateurRepository.findByEmail("prof@mycampus.ma")).thenReturn(Optional.of(utilisateur));
        when(refreshTokenMetier.creerRefreshTokenPourlaPremierFois("prof@mycampus.ma", "JUnit", "127.0.0.1"))
                .thenReturn(refreshToken);
        when(jwtEncoder.encode(any())).thenReturn(jwt);

        JetonsSortieDTO resultat = controller.connecter(dto, request, response);

        assertNotNull(resultat);
        assertEquals("access-token", resultat.getAccess_token());
        assertTrue(response.getHeader("Set-Cookie").contains("refresh_token=refresh-token"));
        verify(limitationTentativesConnexionService).verifierAutorisation("prof@mycampus.ma", "127.0.0.1");
        verify(limitationTentativesConnexionService).reinitialiser("prof@mycampus.ma", "127.0.0.1");
    }

    @Test
    void connecter_RecaptchaInvalide_ThrowsExceptionEtEnregistreEchec() {
        ConnectionDTO dto = ConnectionDTO.builder()
                .email("prof@mycampus.ma")
                .motDePasse("secret")
                .recaptchaToken("")
                .build();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(recaptchaService.validateRecaptcha("")).thenReturn(false);

        SecuriteException exception = assertThrows(
                SecuriteException.class,
                () -> controller.connecter(dto, request, response)
        );

        assertEquals("Validation reCAPTCHA echouee. Veuillez reessayer.", exception.getMessage());
        verify(limitationTentativesConnexionService).enregistrerEchec("prof@mycampus.ma", "127.0.0.1");
    }

    @Test
    void connecter_MotDePasseIncorrect_ThrowsExceptionEtEnregistreEchec() {
        ConnectionDTO dto = ConnectionDTO.builder()
                .email("prof@mycampus.ma")
                .motDePasse("wrong")
                .recaptchaToken("captcha-ok")
                .build();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(recaptchaService.validateRecaptcha("captcha-ok")).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> controller.connecter(dto, request, response)
        );

        verify(limitationTentativesConnexionService).enregistrerEchec("prof@mycampus.ma", "127.0.0.1");
    }

    @Test
    void rafraichir_RefreshTokenValide_RetourneNouveauTokenEtMetAJourCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refresh_token", "old-refresh"));
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit");
        MockHttpServletResponse response = new MockHttpServletResponse();

        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("prof@mycampus.ma")
                .password("encoded")
                .authorities("ROLE_PROFESSEUR")
                .build();

        when(refreshTokenMetier.rotationRefreshToken("old-refresh", "JUnit", "127.0.0.1"))
                .thenReturn(new RotationResult("prof@mycampus.ma", "new-refresh"));
        when(userDetailsServiceImpl.loadUserByUsername("prof@mycampus.ma")).thenReturn(userDetails);
        when(utilisateurRepository.findByEmail("prof@mycampus.ma")).thenReturn(Optional.of(utilisateur));
        when(jwtEncoder.encode(any())).thenReturn(jwt);

        JetonsSortieDTO resultat = controller.rafraichir(request, response);

        assertEquals("access-token", resultat.getAccess_token());
        assertTrue(response.getHeader("Set-Cookie").contains("refresh_token=new-refresh"));
    }

    @Test
    void rafraichir_SansCookie_ThrowsException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        SecuriteException exception = assertThrows(
                SecuriteException.class,
                () -> controller.rafraichir(request, response)
        );

        assertEquals("Refresh token manquant", exception.getMessage());
    }
}
