package com.notic.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notic.response.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.setStatus(HttpStatus.FORBIDDEN.value());
        System.out.println("Access denied");
        System.out.println("Access denied");
        System.out.println("Access denied");
        System.out.println("Access denied");
        ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.FORBIDDEN.getReasonPhrase(),  accessDeniedException.getMessage(), HttpStatus.FORBIDDEN.value());

        new ObjectMapper().writeValue(response.getWriter(), errorResponse);
    }
}
