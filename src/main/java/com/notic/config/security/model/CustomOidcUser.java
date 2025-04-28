package com.notic.config.security.model;

import com.notic.entity.Role;
import com.notic.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import java.util.Collection;
import java.util.Set;

@Getter
public class CustomOidcUser extends DefaultOidcUser {
    private final long id;
    private final Set<String> roleNames;

    public CustomOidcUser(OidcIdToken idToken, Collection<? extends GrantedAuthority> authorities, long userId, Set<String> roleNames) {
        super(authorities, idToken, "email");

        this.id = userId;
        this.roleNames = roleNames;
    }
}
