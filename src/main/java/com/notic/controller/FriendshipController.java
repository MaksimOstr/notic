package com.notic.controller;

import com.notic.config.security.model.CustomJwtUser;
import com.notic.projection.FriendshipProjection;
import com.notic.service.FriendshipService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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


    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "You received list of actual friendships"
            )
    })
    @GetMapping
    public ResponseEntity<Page<FriendshipProjection>> getFriendshipsByUserId(
            @AuthenticationPrincipal CustomJwtUser principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FriendshipProjection> friendshipsPage = friendshipService.getFriendships(principal.getId(), pageable);

        return ResponseEntity.ok(friendshipsPage);
    }


    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "If friendship was successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "If friendship id is not correct or you are not a part of it"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFriendship(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomJwtUser principal
    ) {
        friendshipService.deleteFriendship(id, principal.getId());

        return ResponseEntity.ok().build();
    }
}
