    package com.mycampus.backend.Securite;


    import com.mycampus.backend.metier.security.UserDetailsServiceImpl;
    import com.mycampus.backend.web.errors.JsonAccessDeniedHandler;
    import com.mycampus.backend.web.errors.JsonAuthenticationEntryPoint;
    import com.nimbusds.jose.jwk.source.ImmutableSecret;
    import lombok.RequiredArgsConstructor;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.core.annotation.Order;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.authentication.ProviderManager;
    import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
    import org.springframework.security.config.Customizer;
    import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
    import org.springframework.security.oauth2.jwt.JwtDecoder;
    import org.springframework.security.oauth2.jwt.JwtEncoder;
    import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
    import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
    import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
    import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.web.cors.CorsConfiguration;
    import org.springframework.web.cors.CorsConfigurationSource;
    import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
    import org.springframework.web.filter.CorsFilter;

    import javax.crypto.spec.SecretKeySpec;


    @Configuration
    @EnableWebSecurity
    @EnableMethodSecurity
    @RequiredArgsConstructor
    public class ConfigurationSecurite {

         private final  UserDetailsServiceImpl userDetailsServiceImpl;


        @Value("${jwt.secretKey}")
        private String secretKey;



        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
            grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");
            grantedAuthoritiesConverter.setAuthorityPrefix("");

            JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
            jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);


            return http
                    .headers(header -> header.frameOptions(frame -> frame.disable()))
                    /*.exceptionHandling(ex -> ex
                            .authenticationEntryPoint(new JsonAuthenticationEntryPoint())
                            .accessDeniedHandler(new JsonAccessDeniedHandler())
                    )*/


                    .csrf(csrf -> csrf.disable())
                    .cors(Customizer.withDefaults())
                    .httpBasic(Customizer.withDefaults())

                    .authorizeHttpRequests(auth -> auth
                             .requestMatchers("/auth/connecter", "/auth/rafraichir-jeton", "/auth/deconnexion").permitAll()

                             .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").authenticated()

                             .anyRequest().authenticated()
                    )
                    .oauth2ResourceServer(oa -> oa.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                    .userDetailsService(userDetailsServiceImpl)
                    .build();
        }
        @Bean
        public JwtEncoder jwtEncoder() {

            return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey.getBytes()));
        }

        @Bean
        public JwtDecoder jwtDecoder() {

            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA512");
            return NimbusJwtDecoder.withSecretKey(secretKeySpec).macAlgorithm(MacAlgorithm.HS512).build();
        }

        @Bean
        public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder, UserDetailsServiceImpl userDetailsServiceImpl) {
            DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsServiceImpl);
            daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
            return new ProviderManager(daoAuthenticationProvider);

        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowCredentials(true);
            config.addAllowedOrigin("http://localhost:5173");

            config.addAllowedHeader("*");
            config.addAllowedMethod("*");

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", config);
            return source;
        }


    }
