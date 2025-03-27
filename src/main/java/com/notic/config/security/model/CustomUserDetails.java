package com.notic.config.security.model;

import com.notic.entity.Role;
import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;


public class CustomUserDetails implements UserDetails, CredentialsContainer {

    private final String email;
    private String password;
    private final boolean accountNonLocked;
    private final boolean enabled;
    private final Set<String> authorities;
    @Getter
    private final long userId;

    public CustomUserDetails(String email, String password, boolean accountNonLocked, boolean enabled, long userId, Set<String> authorities) {
        this.email = email;
        this.password = password;
        this.accountNonLocked = accountNonLocked;
        this.authorities = authorities;
        this.enabled = enabled;
        this.userId = userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }

}
