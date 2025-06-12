package com.notic.config.security.model;

import com.notic.entity.Role;
import com.notic.entity.User;
import com.notic.projection.UserWithRolesProjection;
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
    private final Collection<? extends GrantedAuthority> authorities;
    @Getter
    private final long userId;

    public CustomUserDetails(UserWithRolesProjection user) {
        this.email = user.email();
        this.password = user.password();
        this.accountNonLocked = user.accountNonLocked();
        this.enabled = user.enabled();
        this.userId = user.id();
        this.authorities = mapToAuthorities(user.getRoleNamesSet());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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

    private Collection<? extends GrantedAuthority> mapToAuthorities(Set<String> roles) {
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}
