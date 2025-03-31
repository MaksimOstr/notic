package com.notic.config.security.handler;

import com.notic.config.security.model.CustomOidcUser;
import com.notic.entity.User;
import com.notic.service.JwtService;
import com.notic.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;


@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOidcUser oidcUser = (CustomOidcUser) authentication.getPrincipal();

        System.out.println(authentication.getCredentials());
        System.out.println(authentication.getAuthorities());
        System.out.println();

        User user = oidcUser.getUser();

        String accessToken = jwtService.getJwsToken(authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(), 123);
        String refreshToken = refreshTokenService.getRefreshToken(user);
        Cookie refreshTokenCookie = refreshTokenService.getRefreshTokenCookie(refreshToken);

        response.addCookie(refreshTokenCookie);
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json");
        response.getWriter().write(accessToken);
    }
}
