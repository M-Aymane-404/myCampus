package com.mycampus.backend.metier.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@AllArgsConstructor
 @Builder
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String cin;
    private final boolean active;
    private final String password;
    private final  Collection<GrantedAuthority> authorities;



    @Override public String getUsername() {
        return email;
    }
    @Override public String getPassword() {
        return password;
    }

    @Override public boolean isAccountNonExpired() {
        return true;
    }
    @Override public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override public boolean isAccountNonLocked() {
        return active;
    }
    @Override public boolean isEnabled() {
        return active;
    }
}
