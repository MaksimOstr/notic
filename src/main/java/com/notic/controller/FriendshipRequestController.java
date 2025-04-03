package com.notic.controller;


import com.notic.dto.SendFriendshipRequestDto;
import com.notic.projection.JwtAuthUserProjection;
import com.notic.service.FriendshipRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/friendship-requests")
@RequiredArgsConstructor
public class FriendshipRequestController {
    private final FriendshipRequestService friendshipRequestService;


    @PostMapping()
    public ResponseEntity<?> sendFriendshipRequest(
            @AuthenticationPrincipal JwtAuthUserProjection user,
            @RequestBody @Valid SendFriendshipRequestDto friendshipRequest
    ) {
        friendshipRequestService.createRequest(user.getId(), friendshipRequest.receiverId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptFriendshipRequest(
            @PathVariable("id") long requestId,
            @AuthenticationPrincipal JwtAuthUserProjection user
    ) {
        friendshipRequestService.acceptFriendshipRequest(requestId, user.getId());
        return ResponseEntity.ok().build();
    }
}
