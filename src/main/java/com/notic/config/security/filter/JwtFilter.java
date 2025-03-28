package com.notic.config.security.filter;

import com.notic.exception.AuthenticationFlowException;
import com.notic.mapper.UserMapper;
import com.notic.projection.JwtAuthUserProjection;
import com.notic.projection.UserCredentialsProjection;
import com.notic.config.security.handler.CustomAuthenticationEntryPoint;
import com.notic.config.security.model.CustomUserDetails;
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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
//Improve

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    private final JwtService jwtService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        if(SecurityContextHolder.getContext().getAuthentication() == null && token != null) {
            try {
                String userId = jwtService.extractUserId(token);
                Collection<String> roles = jwtService.extractAuthorities(token);
                JwtAuthUserProjection user = userService.getUserForJwtAuth(Long.parseLong(userId))
                        .orElseThrow(() -> new AuthenticationFlowException("Authentication failed"));

                if(!user.isAccountNonLocked()) {
                    throw new LockedException("User account is locked");
                }

                authenticateUser(user, roles, request);
            } catch(JwtException | AuthenticationFlowException e) {
                logger.warn("User not found or invalid token");
                SecurityContextHolder.clearContext();
                customAuthenticationEntryPoint.commence(request, response, null);
                return;
            } catch (AuthenticationException e) {
                logger.warn("User locked");
                SecurityContextHolder.clearContext();
                customAuthenticationEntryPoint.commence(request, response, e);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(JwtAuthUserProjection user, Collection<String> roles, HttpServletRequest request) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        CustomUserDetails userDetails = userMapper.toCustomUserDetails(user);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                roles.stream().map(SimpleGrantedAuthority::new).toList()
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
