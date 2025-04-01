package com.notic.config.security.service;

import com.notic.config.security.model.CustomOidcUser;
import com.notic.entity.User;
import com.notic.enums.AuthProviderEnum;
import com.notic.service.RoleService;
import com.notic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserService userService;
    private final RoleService roleService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        OidcIdToken idToken = userRequest.getIdToken();
        String email = idToken.getEmail();
        String username = idToken.getClaimAsString("name");

        User user = userService.getUserByEmailWithRoles(email)
                .orElseGet(() -> {
                    User newUser = new User(
                            username,
                            email,
                            Set.of(roleService.getDefaultRole()),
                            AuthProviderEnum.GOOGLE
                    );
                    return userService.saveUser(newUser);
                });

        if(user.getAuthProvider() == AuthProviderEnum.LOCAL) {
            OAuth2Error oauth2Error = new OAuth2Error("This account was created by another provider");
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }

        return new CustomOidcUser(
                userRequest.getIdToken(),
                List.of(),
                user
        );
    }
}
