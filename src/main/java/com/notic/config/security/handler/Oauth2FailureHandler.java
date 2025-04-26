package com.notic.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notic.dto.response.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Oauth2FailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.UNAUTHORIZED.getReasonPhrase(),  exception.getMessage(), HttpStatus.UNAUTHORIZED.value());

        new ObjectMapper().writeValue(response.getWriter(), errorResponse);
    }
}
