package com.notic.security.filter;

import com.notic.exception.EntityDoesNotExistsException;
import com.notic.mapper.UserMapper;
import com.notic.projection.UserCredentialsProjection;
import com.notic.security.model.CustomUserDetails;
import com.notic.service.JwtService;
import com.notic.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

//Improve

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    private final JwtService jwtService;
    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        if(SecurityContextHolder.getContext().getAuthentication() == null && token != null) {
            try {
                String email = jwtService.extractEmail(token);
                UserCredentialsProjection user = userService.getUserForAuth(email);

                if(!user.isAccountNonLocked()) {
                    throw new LockedException("User account is locked");
                }

                authenticateUser(user, request);
            } catch(JwtException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } catch (EntityDoesNotExistsException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(e.getMessage());
                return;
            }

        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(UserCredentialsProjection user, HttpServletRequest request) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        CustomUserDetails userDetails = userMapper.toCustomUserDetails(user);
        userDetails.eraseCredentials();
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HEADER_NAME);
        boolean isBearer = StringUtils.startsWithIgnoreCase(authHeader, BEARER_PREFIX);
        if (StringUtils.hasText(authHeader) && isBearer) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            if (StringUtils.hasText(token)) {
                return token;
            }
        }
        return null;
    }
}
