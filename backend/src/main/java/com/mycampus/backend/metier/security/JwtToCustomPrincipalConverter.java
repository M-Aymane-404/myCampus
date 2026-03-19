package com.mycampus.backend.metier.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;

public class JwtToCustomPrincipalConverter implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter authoritiesConverter;

    public JwtToCustomPrincipalConverter(JwtGrantedAuthoritiesConverter authoritiesConverter) {
        this.authoritiesConverter = authoritiesConverter;
    }

    @Override
    public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);

        Long userId = jwt.getClaim("userId");
        String email = jwt.getSubject();
        Boolean active = jwt.getClaim("active");

        CustomUserDetails principal = CustomUserDetails.builder()
                .id(userId)
                .email(email)
                .cin(null)
                .active(Boolean.TRUE.equals(active))
                .password("")
                .authorities(authorities)
                .build();
        return new UsernamePasswordAuthenticationToken(principal, "N/A", authorities);
    }
}
