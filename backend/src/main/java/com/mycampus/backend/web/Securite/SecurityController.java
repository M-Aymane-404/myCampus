package com.mycampus.backend.web.Securite;

import com.mycampus.backend.dto.securite.entree.ConnectionDTO;
import com.mycampus.backend.dto.securite.entree.ConnectionMobileDTO;
import com.mycampus.backend.dto.securite.sortie.JetonsSortieDTO;
import com.mycampus.backend.dto.securite.sortie.RotationResult;
import com.mycampus.backend.entites.RefreshToken;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.exceptions.SecuriteException;
import com.mycampus.backend.metier.security.CustomUserDetails;
import com.mycampus.backend.metier.security.RefreshTokenMetier;
import com.mycampus.backend.metier.security.UserDetailsServiceImpl;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import com.mycampus.backend.metier.security.RecaptchaService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class SecurityController {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final RefreshTokenMetier refreshTokenMetier;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final UtilisateurRepository utilisateurRepository;
    private final RecaptchaService recaptchaService;

    @Value("${app.security.cookie-secure:false}")
    private boolean cookieSecure;

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal CustomUserDetails user) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new SecuriteException("Utilisateur introuvable"));

        String role = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .findFirst()
                .orElse("ROLE_ETUDIANT");

        return Map.of(
                "id", utilisateur.getId(),
                "email", utilisateur.getEmail(),
                "nom", utilisateur.getNom(),
                "prenom", utilisateur.getPrenom(),
                "active", utilisateur.getActive(),
                "role", role,
                "authorities", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
        );
    }

    @GetMapping("/profile")
    public Map<String, Object> profile(Authentication authentication) {
        return Map.of(
                "email", authentication.getName(),
                "authorities", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );
    }

    @PostMapping("/connecter")
    public JetonsSortieDTO connecter(@Valid @RequestBody ConnectionDTO connectionDTO,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        if (!recaptchaService.validateRecaptcha(connectionDTO.getRecaptchaToken())) {
            throw new SecuriteException("Validation reCAPTCHA echouee. Veuillez reessayer.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(connectionDTO.getEmail(), connectionDTO.getMotDePasse())
        );

        return creerSession(authentication, request, response);
    }

    @PostMapping("/connecter-mobile")
    public JetonsSortieDTO connecterMobile(@Valid @RequestBody ConnectionMobileDTO connectionDTO,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(connectionDTO.getEmail(), connectionDTO.getMotDePasse())
        );

        return creerSession(authentication, request, response);
    }

    @PostMapping("/rafraichir-jeton")
    public JetonsSortieDTO rafraichir(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, "refresh_token");
        if (cookie == null || cookie.getValue().isBlank()) {
            throw new SecuriteException("Refresh token manquant");
        }

        String refreshToken = cookie.getValue();
        String userAgent = request.getHeader("User-Agent");
        String ip = extraireIp(request);

        RotationResult nouveau = refreshTokenMetier.rotationRefreshToken(refreshToken, userAgent, ip);
        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(nouveau.getEmail());
        Utilisateur utilisateur = utilisateurRepository.findByEmail(nouveau.getEmail())
                .orElseThrow(() -> new SecuriteException("Utilisateur introuvable"));

        String accessToken = genererAccessToken(userDetails.getUsername(), utilisateur.getId(), utilisateur.getActive(), userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" ")));

        ResponseCookie nouveauCookie = ResponseCookie.from("refresh_token", nouveau.getRefreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/auth")
                .maxAge(7 * 24 * 60 * 60)
                .build();

        response.addHeader("Set-Cookie", nouveauCookie.toString());
        return new JetonsSortieDTO(accessToken);
    }

    @PostMapping("/deconnexion")
    public Map<String, String> deconnexion(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, "refresh_token");
        if (cookie != null && !cookie.getValue().isBlank()) {
            refreshTokenMetier.revoquerFamille(cookie.getValue());
        }

        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/auth")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", deleteCookie.toString());

        return Map.of("message", "Deconnexion reussie");
    }

    private JetonsSortieDTO creerSession(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new SecuriteException("Utilisateur introuvable"));

        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        String accessToken = genererAccessToken(authentication.getName(), utilisateur.getId(), utilisateur.getActive(), scope);

        String userAgent = request.getHeader("User-Agent");
        String ip = extraireIp(request);
        RefreshToken refresh = refreshTokenMetier.creerRefreshTokenPourlaPremierFois(authentication.getName(), userAgent, ip);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", refresh.getToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/auth")
                .maxAge(7 * 24 * 60 * 60)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        return JetonsSortieDTO.builder().access_token(accessToken).build();
    }

    private String genererAccessToken(String sujet, Long userId, Boolean actif, String scope) {
        Instant instant = Instant.now();
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .issuedAt(instant)
                .expiresAt(instant.plus(10, ChronoUnit.MINUTES))
                .subject(sujet)
                .claim("scope", scope)
                .claim("userId", userId)
                .claim("active", actif)
                .build();
        JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS512).build(),
                jwtClaimsSet
        );
        return jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
    }

    private String extraireIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
