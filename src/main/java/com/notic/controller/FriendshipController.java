package com.notic.controller;


import com.notic.dto.FriendshipInfoDto;
import com.notic.entity.Friendship;
import com.notic.projection.FriendshipDto;
import com.notic.projection.JwtAuthUserProjection;
import com.notic.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/friendships")
@RequiredArgsConstructor
public class FriendshipController {
    private final FriendshipService friendshipService;

    @GetMapping
    public ResponseEntity<Page<FriendshipDto>> getFriendshipsByUserId(
            @AuthenticationPrincipal JwtAuthUserProjection user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        System.out.println(user.getId());
        Pageable pageable = PageRequest.of(page, size);
        Page<FriendshipDto> friendshipsPage = friendshipService.getFriendships(user.getId(), pageable);

        return ResponseEntity.ok(friendshipsPage);
    }
}
