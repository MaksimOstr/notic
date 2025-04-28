package com.notic.config.security.handler;

import com.notic.config.security.JwtConfig;
import com.notic.config.security.model.CustomOidcUser;
import com.notic.dto.response.TokenResponse;
import com.notic.entity.Role;
import com.notic.entity.User;
import com.notic.service.CookieService;
import com.notic.service.JwtService;
import com.notic.service.RefreshTokenService;
import com.notic.service.TokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;


@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final CookieService cookieService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOidcUser oidcUser = (CustomOidcUser) authentication.getPrincipal();

        User user = oidcUser.getUser();

        TokenResponse tokenResponse = tokenService.getTokenPair(user);
        Cookie refreshTokenCookie = cookieService.createRefreshTokenCookie(tokenResponse.refreshToken());

        response.addCookie(refreshTokenCookie);
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json");
        response.getWriter().write(tokenResponse.accessToken());
    }
}
