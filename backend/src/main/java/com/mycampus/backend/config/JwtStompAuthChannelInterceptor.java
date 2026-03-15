package com.mycampus.backend.config;

import java.util.Collection;
import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
public class JwtStompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final JwtGrantedAuthoritiesConverter authoritiesConverter;

    public JwtStompAuthChannelInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
        this.authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        this.authoritiesConverter.setAuthoritiesClaimName("scope");
        this.authoritiesConverter.setAuthorityPrefix("");
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        String authHeader = firstNonBlank(
                accessor.getFirstNativeHeader("Authorization"),
                accessor.getFirstNativeHeader("authorization"));
        if (authHeader == null) {
            throw new IllegalArgumentException("Missing Authorization header in STOMP CONNECT");
        }

        String token = extractBearerToken(authHeader);
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(token);
        } catch (JwtException ex) {
            throw new IllegalArgumentException("Invalid JWT in STOMP CONNECT", ex);
        }

        String principalName = jwt.getSubject();
        if (principalName == null || principalName.isBlank()) {
            throw new IllegalArgumentException("JWT subject is missing in STOMP CONNECT");
        }

        Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);
        if (authorities == null) {
            authorities = List.of();
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(principalName, null, authorities);
        accessor.setUser(authentication);
        return message;
    }

    private static String extractBearerToken(String authHeader) {
        String value = authHeader.trim();
        if (value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return value.substring(7).trim();
        }
        throw new IllegalArgumentException("Authorization header must use Bearer token");
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
