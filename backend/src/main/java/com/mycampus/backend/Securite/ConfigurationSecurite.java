package com.mycampus.backend.Securite;

import com.mycampus.backend.metier.security.JwtToCustomPrincipalConverter;
import com.mycampus.backend.metier.security.UserDetailsServiceImpl;
import com.mycampus.backend.web.errors.JsonAccessDeniedHandler;
import com.mycampus.backend.web.errors.JsonAuthenticationEntryPoint;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class ConfigurationSecurite {

    private final UserDetailsServiceImpl userDetailsServiceImpl;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        var customConverter = new JwtToCustomPrincipalConverter(grantedAuthoritiesConverter);

        return http
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new JsonAuthenticationEntryPoint())
                        .accessDeniedHandler(new JsonAccessDeniedHandler())
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(Customizer.withDefaults())
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                                .preload(true)
                        )
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                        "base-uri 'self'; " +
                                        "form-action 'self'; " +
                                        "frame-ancestors 'none'; " +
                                        "object-src 'none'; " +
                                        "script-src 'self'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data: blob:; " +
                                        "font-src 'self' data:; " +
                                        "connect-src 'self' " + corsConnectSrc() + ";"
                        ))
                )
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/connecter", "/auth/connecter-mobile", "/auth/rafraichir-jeton", "/auth/deconnexion").permitAll()
                        .requestMatchers("/ws/**", "/ws-sockjs/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oa -> oa.jwt(jwt -> jwt.jwtAuthenticationConverter(customConverter)))
                .userDetailsService(userDetailsServiceImpl)
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(decodedSecret()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(decodedSecret(), "HmacSHA512");
        return NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            PasswordEncoder passwordEncoder,
            UserDetailsServiceImpl userDetailsServiceImpl
    ) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsServiceImpl);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(daoAuthenticationProvider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(resolveAllowedOrigins());
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setExposedHeaders(List.of("Content-Disposition"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private byte[] decodedSecret() {
        byte[] decoded = Base64.getDecoder().decode(secretKey);
        if (decoded.length < 64) {
            throw new IllegalStateException("JWT_SECRET doit contenir au moins 64 octets en base64 pour HS512.");
        }
        return decoded;
    }

    private List<String> resolveAllowedOrigins() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
    }

    private String corsConnectSrc() {
        return resolveAllowedOrigins().stream().collect(Collectors.joining(" "));
    }
}
