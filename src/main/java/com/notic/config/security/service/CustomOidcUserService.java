package com.notic.config.security.service;

import com.notic.config.security.model.CustomOidcUser;
import com.notic.dto.CreateProviderUserDto;
import com.notic.entity.Role;
import com.notic.entity.User;
import com.notic.enums.AuthProviderEnum;
import com.notic.service.UserService;
import jakarta.transaction.Transactional;
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
import java.util.stream.Collectors;

import static com.notic.utils.UserUtils.mapUserRoles;


@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserService userService;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcIdToken idToken = userRequest.getIdToken();
        String email = idToken.getEmail();
        String username = idToken.getClaimAsString("name");
        String avatarUrl = idToken.getPicture();
        CreateProviderUserDto createProviderUserDto = new CreateProviderUserDto(
                AuthProviderEnum.GOOGLE,
                email,
                username,
                avatarUrl
        );
        User user = userService.createProviderUser(createProviderUserDto);

        if(user.getAuthProvider() != AuthProviderEnum.GOOGLE) {
            OAuth2Error oauth2Error = new OAuth2Error("This account was created by another provider");
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }

        return new CustomOidcUser(
                userRequest.getIdToken(),
                List.of(),
                user.getId(),
                mapUserRoles(user)
        );
    }
}
