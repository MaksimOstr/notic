package com.notic.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notic.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;


@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.UNAUTHORIZED.getReasonPhrase(),  authException.getMessage(), HttpStatus.UNAUTHORIZED.value());

        new ObjectMapper().writeValue(response.getWriter(), errorResponse);
    }
}
