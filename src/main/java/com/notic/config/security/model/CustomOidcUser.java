package com.notic.config.security.model;

import com.notic.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.util.Collection;

@Getter
public class CustomOidcUser extends DefaultOidcUser {
    private final User user;

    public CustomOidcUser(OidcIdToken idToken, Collection<? extends GrantedAuthority> authorities, User user) {
        super(authorities, idToken, "email");
        this.user = user;
    }
}
