package com.mycampus.backend.web.Securite;

import com.mycampus.backend.dto.securite.entree.ConnectionDTO;
import com.mycampus.backend.dto.securite.entree.RafraichirTokenDTO;
import com.mycampus.backend.dto.securite.sortie.JetonsSortieDTO;
import com.mycampus.backend.entites.RefreshToken;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.metier.security.RefreshTokenMetier;
import com.mycampus.backend.metier.security.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
 import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class SecurityController {

     private final AuthenticationManager authenticationManager;
     private final JwtEncoder jwtEncoder;
     private final RefreshTokenMetier refreshTokenMetier;
     private final UserDetailsServiceImpl userDetailsServiceImpl;

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
    public JetonsSortieDTO connecter(@Valid @RequestBody ConnectionDTO connectionDTO, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(connectionDTO.getEmail(),connectionDTO.getMotDePasse())
        );


        Instant instant = Instant.now();
        String scope  = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .issuedAt(instant)
                .expiresAt(instant.plus(15, ChronoUnit.MINUTES))

                .subject(authentication.getName())
                .claim("scope", scope)
                .build();
        JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS512).build(),jwtClaimsSet);
        String jwt = jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
        String userAgent = request.getHeader("User-Agent");
        String ip = extraireIp(request);


        RefreshToken refresh = refreshTokenMetier.creerRefreshToken(authentication.getName(), userAgent, ip);

         return JetonsSortieDTO.builder()
                 .access_token(jwt)
                 .refresh_token(refresh.getToken())
                 .build();

    }

    @PostMapping("/rafraichir-jeton")
    public JetonsSortieDTO rafraichir(@Valid @RequestBody RafraichirTokenDTO dto,
                                      HttpServletRequest request) {

        String userAgent = request.getHeader("User-Agent");
        String ip = extraireIp(request);


        RefreshToken nouveau = refreshTokenMetier.rotationRefreshToken(dto.getRefreshToken(), userAgent, ip);

        Utilisateur utilisateur = nouveau.getUtilisateur();


        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(utilisateur.getEmail());

        Instant instant = Instant.now();
        String scope  = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .issuedAt(instant)
                .expiresAt(instant.plus(15, ChronoUnit.MINUTES))

                .subject(userDetails.getUsername())
                .claim("scope", scope)
                .build();
        JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS512).build(),jwtClaimsSet);
        String accessToken = jwtEncoder.encode(jwtEncoderParameters).getTokenValue();

        return new JetonsSortieDTO(accessToken, nouveau.getToken());
    }

    @PostMapping("/deconnexion")
    public Map<String, String> deconnexion(@Valid @RequestBody RafraichirTokenDTO dto) {
        refreshTokenMetier.revoquer(dto);
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
