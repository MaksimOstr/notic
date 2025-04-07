package com.notic.unit.controller;

import com.notic.advice.FriendshipControllerAdvice;
import com.notic.advice.GlobalControllerAdvice;
import com.notic.config.security.filter.JwtFilter;
import com.notic.controller.FriendshipController;
import com.notic.dto.JwtAuthUserDto;
import com.notic.exception.FriendshipException;
import com.notic.projection.FriendshipProjection;
import com.notic.repository.FriendshipRepository;
import com.notic.service.FriendshipService;
import com.notic.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.Instant;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = FriendshipController.class)
public class FriendshipControllerTest {


    @MockitoBean
    private FriendshipService friendshipService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private FriendshipRepository friendshipRepository;

    @MockitoBean
    private UserService userService;

    private MockMvc mockMvc;

    private final long userId = 1L;
    private final JwtAuthUserDto user = new JwtAuthUserDto(userId);

    @BeforeEach
    void setup() {
        FriendshipController friendshipController = new FriendshipController(friendshipService);
        GlobalControllerAdvice globalExceptionHandler = new GlobalControllerAdvice();
        FriendshipControllerAdvice friendshipControllerAdvice = new FriendshipControllerAdvice();
        mockMvc = MockMvcBuilders.standaloneSetup(friendshipController)
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
    class GetFriendshipsByUserId {
        private final int page = 0;
        private final int pageSize = 10;

        private final long friendId = 1;
        private final String friendName = "Bob";
        private final String avatarUrl = "url";
        private final Instant createdAt = Instant.now();

        private final FriendshipProjection projection = new FriendshipProjection(
                friendId,
                friendName,
                avatarUrl,
                createdAt
        );

        private final Page<FriendshipProjection> resultsPages = new PageImpl<>(
                List.of(projection),
                PageRequest.of(page, pageSize),
                1L
        );

        @Test
        void shouldReturnPageOfFriendships() throws Exception {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(user, null, List.of())
            );

            when(friendshipService.getFriendships(anyLong(), any(Pageable.class))).thenReturn(resultsPages);

            mockMvc.perform(get("/friendships"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].friendId").value(projection.friendId()))
                    .andExpect(jsonPath("$.content[0].friendName").value(projection.friendName()))
                    .andExpect(jsonPath("$.content[0].friendAvatar").value(projection.friendAvatar()));

            verify(friendshipService, times(1)).getFriendships(anyLong(), any(Pageable.class));
        }
    }

    @Nested
    class DeleteFriendship {
        private final int friendshipId = 1;

        @Test
        void shouldDeleteFriendship() throws Exception {

            mockMvc.perform(delete("/friendships/{friendshipId}", friendshipId))
                    .andExpect(status().isOk());

            verify(friendshipService, times(1)).deleteFriendship(friendshipId, userId);
        }


        @Test
        void shouldThrowFriendshipExceptionAndHandle() throws Exception {

            String error = "error";

            doThrow(new FriendshipException(error)).when(friendshipService).deleteFriendship(anyLong(), anyLong());

            mockMvc.perform(delete("/friendships/{friendshipId}", friendshipId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(error))
                    .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

            verify(friendshipService, times(1)).deleteFriendship(friendshipId, userId);
        }
    }

}
