package com.mycampus.backend.web.Securite;

import com.mycampus.backend.dto.securite.entree.ConnectionDTO;
import com.mycampus.backend.dto.securite.entree.RafraichirTokenDTO;
import com.mycampus.backend.dto.securite.sortie.JetonsSortieDTO;
import com.mycampus.backend.dto.securite.sortie.RotationResult;
import com.mycampus.backend.entites.RefreshToken;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.exceptions.SecuriteException;
import com.mycampus.backend.metier.security.CustomUserDetails;
import com.mycampus.backend.metier.security.RefreshTokenMetier;
import com.mycampus.backend.metier.security.RefreshTokenMetierImpl;
import com.mycampus.backend.metier.security.UserDetailsServiceImpl;
import com.mycampus.backend.repositorys.UtilisateurRepository;
import com.mycampus.backend.services.RecaptchaService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;
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
    private final SimpMessagingTemplate messagingTemplate;
    private final UtilisateurRepository utilisateurRepository;
    private final RecaptchaService recaptchaService;



    private final boolean cookieSecure =  false;

    @GetMapping("/me")
    public Map<String,Object> me(@AuthenticationPrincipal CustomUserDetails user) {
        return Map.of(
                "id", user.getId(),
                "email", user.getUsername(),
                "cin", user.getCin(),
                "active", user.isEnabled(),
                "authorities", user.getAuthorities().stream().map(a -> a.getAuthority()).toList()
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
        
        // Valider le token reCAPTCHA
        log.info("reCAPTCHA token received: {}", connectionDTO.getRecaptchaToken());
        if (!recaptchaService.validateRecaptcha(connectionDTO.getRecaptchaToken())) {
            log.error("reCAPTCHA validation failed for token: {}", connectionDTO.getRecaptchaToken());
            throw new SecuriteException("Validation reCAPTCHA échouée. Veuillez réessayer.");
        }
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(connectionDTO.getEmail(),connectionDTO.getMotDePasse())
        );
        Utilisateur u = utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new SecuriteException("Utilisateur introuvable"));

        Instant instant = Instant.now();
        String scope  = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .issuedAt(instant)
                .expiresAt(instant.plus(20, ChronoUnit.MINUTES))

                .subject(authentication.getName())
                .claim("scope", scope)
                .claim("userId", u.getId())
                .claim("cin", u.getCin())
                .claim("active", u.getActive())

                .build();
        JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS512).build(),jwtClaimsSet);
        String jwt = jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
        String userAgent = request.getHeader("User-Agent");
        String ip = extraireIp(request);


        RefreshToken refresh = refreshTokenMetier.creerRefreshTokenPourlaPremierFois(authentication.getName(), userAgent, ip);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", refresh.getToken())
                .httpOnly(true)
                .secure(cookieSecure)              // en prod (https). en local: false
                .sameSite("Lax")           // ou "None" si front/back domaines différents
                .path("/auth")             // limite l’envoi du cookie aux endpoints auth
                .maxAge(7 * 24 * 60 * 60)  // 7 jours
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        log.info("cokokie in  connect " + cookie);

        return JetonsSortieDTO.builder()
                .access_token(jwt)
                 .build();


    }

    @PostMapping("/rafraichir-jeton")
    public JetonsSortieDTO rafraichir(
                                      HttpServletRequest request,HttpServletResponse response) {

        Cookie cookie = WebUtils.getCookie(request, "refresh_token");
        if (cookie == null || cookie.getValue().isBlank()) {
            throw new SecuriteException("Refresh token manquant");
        }

        String refreshToken = cookie.getValue();
        String userAgent = request.getHeader("User-Agent");
        String ip = extraireIp(request);


        RotationResult nouveau = refreshTokenMetier.rotationRefreshToken(refreshToken, userAgent, ip);

        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(nouveau.getEmail());

        Utilisateur u = utilisateurRepository.findByEmail(nouveau.getEmail())
                .orElseThrow(() -> new SecuriteException("Utilisateur introuvable"));

        Instant instant = Instant.now();
        String scope  = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .issuedAt(instant)
                .expiresAt(instant.plus(20, ChronoUnit.MINUTES))

                .subject(userDetails.getUsername())
                .claim("scope", scope)
                .claim("userId", u.getId())
                .claim("cin", u.getCin())
                .claim("active", u.getActive())

                .build();
        JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS512).build(),jwtClaimsSet);
        String accessToken = jwtEncoder.encode(jwtEncoderParameters).getTokenValue();

        ResponseCookie nouveauCookieARemplacer = ResponseCookie.from("refresh_token", nouveau.getRefreshToken())
                .httpOnly(true)
                .secure(cookieSecure)              // en prod (https). en local: false
                .sameSite("Lax")           // ou "None" si front/back domaines différents
                .path("/auth")             // limite l’envoi du cookie aux endpoints auth
                .maxAge(7 * 24 * 60 * 60)  // 7 jours
                .build();

        response.addHeader("Set-Cookie", nouveauCookieARemplacer.toString());
        log.info("cokokie in  refresh  " + cookie);

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

        return Map.of("message", "Déconnexion réussie");
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
