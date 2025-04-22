package com.notic.config.security.model;

import com.notic.entity.Role;
import com.notic.entity.User;
import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class CustomUserDetails implements UserDetails, CredentialsContainer {

    @Getter
    private final User user;
    private final String email;
    private String password;
    private final boolean accountNonLocked;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;
    @Getter
    private final long userId;

    public CustomUserDetails(User user) {
        this.user = user;
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.accountNonLocked = user.getAccountNonLocked();
        this.enabled = user.getEnabled();
        this.userId = user.getId();
        this.authorities = mapToAuthorities(user.getRoles());
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

    private Collection<? extends GrantedAuthority> mapToAuthorities(Set<Role> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
    }
}
