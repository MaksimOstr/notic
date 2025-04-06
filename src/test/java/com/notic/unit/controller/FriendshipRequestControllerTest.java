package com.notic.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notic.advice.FriendshipControllerAdvice;
import com.notic.advice.GlobalControllerAdvice;
import com.notic.config.security.filter.JwtFilter;
import com.notic.controller.FriendshipRequestController;
import com.notic.dto.JwtAuthUserDto;
import com.notic.dto.SendFriendshipRequestDto;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.FriendshipException;
import com.notic.projection.FriendshipRequestProjection;
import com.notic.repository.FriendshipRequestRepository;
import com.notic.service.FriendshipRequestService;
import com.notic.service.FriendshipService;
import com.notic.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

@WebMvcTest(controllers = FriendshipRequestControllerTest.class)
public class FriendshipRequestControllerTest {

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private FriendshipRequestService friendshipRequestService;

    @MockitoBean
    private FriendshipRequestRepository friendshipRequestRepository;

    @MockitoBean
    private FriendshipService friendshipService;

    @MockitoBean
    private UserService userService;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final long userId = 22L;
    private final JwtAuthUserDto user = new JwtAuthUserDto(userId);

    @BeforeEach
    void setup() {
        FriendshipRequestController friendshipRequestController = new FriendshipRequestController(friendshipRequestService);
        GlobalControllerAdvice globalExceptionHandler = new GlobalControllerAdvice();
        FriendshipControllerAdvice friendshipControllerAdvice = new FriendshipControllerAdvice();
        mockMvc = MockMvcBuilders.standaloneSetup(friendshipRequestController)
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver()
                )
                .setControllerAdvice(globalExceptionHandler, friendshipControllerAdvice)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of())
        );
    }

    @Nested
    class GetAllFriendshipRequests {
        private final long requestId = 1;
        private final String senderUsername = "Bob";
        private final String senderAvatar = "url";
        private final Instant requestTime = Instant.now();
        int page = 0;
        int pageSize = 10;

        private final FriendshipRequestProjection projection = new FriendshipRequestProjection(
                requestId, senderUsername, senderAvatar, requestTime
        );

        private final Page<FriendshipRequestProjection> resultsPages = new PageImpl<>(
                List.of(projection),
                PageRequest.of(page, pageSize),
                1L
        );

        @Test
        void shouldReturnAllFriendshipRequests() throws Exception {

            when(friendshipRequestService.getAllFriendshipRequests(anyLong(), any(Pageable.class))).thenReturn(resultsPages);

            mockMvc.perform(get("/friendship-requests"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].requestId").value(projection.requestId()))
                    .andExpect(jsonPath("$.content[0].senderUsername").value(projection.senderUsername()))
                    .andExpect(jsonPath("$.content[0].senderAvatar").value(projection.senderAvatar()));

            verify(friendshipRequestService, times(1)).getAllFriendshipRequests(anyLong(), any(Pageable.class));
        }

    }

    @Nested
    class SendFriendshipRequest{
        private final String error = "error";

        @Test
        void shouldSendRequest() throws Exception {
            long friendId = 2L;
            SendFriendshipRequestDto requestDto = new SendFriendshipRequestDto(friendId);

            doNothing().when(friendshipRequestService).createRequest(anyLong(), anyLong());

            mockMvc.perform(post("/friendship-requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk());

            verify(friendshipRequestService, times(1)).createRequest(eq(user.getId()), eq(requestDto.receiverId()));
        }


        @Test
        void shouldHandleFriendshipException() throws Exception {
            SendFriendshipRequestDto requestDto = new SendFriendshipRequestDto(userId);

            doThrow(new FriendshipException(error))
                    .when(friendshipRequestService)
                    .createRequest(anyLong(), anyLong());

            mockMvc.perform(post("/friendship-requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(error))
                    .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

            verify(friendshipRequestService, times(1)).createRequest(eq(requestDto.receiverId()),eq(requestDto.receiverId()));
        }


        @Test
        void shouldHandleEntityAlreadyExistsException() throws Exception {
            SendFriendshipRequestDto requestDto = new SendFriendshipRequestDto(userId);

            doThrow(new EntityAlreadyExistsException(error))
                    .when(friendshipRequestService)
                    .createRequest(anyLong(), anyLong());

            mockMvc.perform(post("/friendship-requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(error))
                    .andExpect(jsonPath("$.code").value(HttpStatus.CONFLICT.getReasonPhrase()))
                    .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()));

            verify(friendshipRequestService, times(1)).createRequest(eq(requestDto.receiverId()),eq(requestDto.receiverId()));
        }
    }

        @Test
        void shouldAcceptRequest() throws Exception {
            long requestId = 2L;

            doNothing().when(friendshipRequestService).acceptFriendshipRequest(anyLong(), anyLong());

            mockMvc.perform(post("/friendship-requests/{id}/accept", requestId))
                    .andExpect(status().isOk());

            verify(friendshipRequestService, times(1)).acceptFriendshipRequest(eq(requestId), eq(user.getId()));
        }


    @Test
    void shouldDeleteRequest() throws Exception {
        long requestId = 2L;

        doNothing().when(friendshipRequestService).rejectFriendshipRequest(anyLong(), anyLong());

        mockMvc.perform(delete("/friendship-requests/{id}", requestId))
                .andExpect(status().isOk());

        verify(friendshipRequestService, times(1)).rejectFriendshipRequest(eq(requestId), eq(user.getId()));
    }
}
