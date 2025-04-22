package com.notic.service;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import static com.notic.utils.RefreshTokenUtils.REFRESH_TOKEN_COOKIE_NAME;


@Service
public class CookieService {

    @Value("${REFRESH_TOKEN_TTL:3600}")
    private int refreshTokenTtl;

    public Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);

        cookie.setHttpOnly(true);
        cookie.setMaxAge(refreshTokenTtl);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "Lax");

        return cookie;
    }

    public Cookie deleteRefreshTokenCookie() {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        return cookie;
    }
}
