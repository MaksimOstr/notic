package com.notic.controller;


import com.notic.dto.CreateFriendshipDto;
import com.notic.entity.Friendship;
import com.notic.projection.JwtAuthUserProjection;
import com.notic.service.FriendshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/friendships")
@RequiredArgsConstructor
public class FriendshipController {
    private final FriendshipService friendshipService;

    @PostMapping
    public ResponseEntity<?> createFriendship(
            @AuthenticationPrincipal JwtAuthUserProjection principal,
            @Valid @RequestBody CreateFriendshipDto body
            ) {
        friendshipService.createFriendship(principal.getId(), body.receiverId());
        return ResponseEntity.ok().build();
    }
}
