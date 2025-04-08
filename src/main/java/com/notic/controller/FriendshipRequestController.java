package com.notic.controller;

import com.notic.dto.JwtAuthUserDto;
import com.notic.dto.SendFriendshipRequestDto;
import com.notic.projection.FriendshipRequestProjection;
import com.notic.response.ApiErrorResponse;
import com.notic.service.FriendshipRequestService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/friendship-requests")
@RequiredArgsConstructor
public class FriendshipRequestController {
    private final FriendshipRequestService friendshipRequestService;


    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Received List of friendship requests"
            )
    })
    @GetMapping
    public ResponseEntity<Page<FriendshipRequestProjection>> getAllFriendshipRequests(
            @AuthenticationPrincipal JwtAuthUserDto user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size

    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FriendshipRequestProjection> requests = friendshipRequestService.getAllFriendshipRequests(user.getId(), pageable);
        return ResponseEntity.ok(requests);
    }


    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "If friendship or request already exist",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "If you try to send friendship request to yourself",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<?> sendFriendshipRequest(
            @AuthenticationPrincipal JwtAuthUserDto user,
            @RequestBody @Valid SendFriendshipRequestDto friendshipRequest
    ) {
        friendshipRequestService.createRequest(user.getId(), friendshipRequest.receiverId());
        return ResponseEntity.ok().build();
    }


    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "If your are not receiver",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "If friendship request was not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptFriendshipRequest(
            @PathVariable("id") long requestId,
            @AuthenticationPrincipal JwtAuthUserDto user
    ) {
        friendshipRequestService.acceptFriendshipRequest(requestId, user.getId());
        return ResponseEntity.ok().build();
    }


    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "If request was not found or you are not a receiver",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFriendshipRequest(
            @PathVariable("id") long requestId,
            @AuthenticationPrincipal JwtAuthUserDto user
    ) {
        friendshipRequestService.rejectFriendshipRequest(requestId, user.getId());

        return ResponseEntity.ok().build();
    }
}
