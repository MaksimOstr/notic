package com.notic.controller;

import com.notic.config.security.model.CustomJwtUser;
import com.notic.dto.CustomPutObjectDto;
import com.notic.dto.response.ApiErrorResponse;
import com.notic.entity.Profile;
import com.notic.service.ProfileService;
import com.notic.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final S3Service s3Service;

    @Value("${AWS_AVATAR_BUCKET_NAME}")
    private String avatarBucketName;

    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile was successfully found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "If profile was not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = {@ExampleObject("{\t\"code\": \"Conflict\",\t\"message\": \"Profile was not found\",\t\"status\": 409}")}
                    )
            )
    })
    @GetMapping("/personal")
    public ResponseEntity<Profile> getPersonalProfile(
            @AuthenticationPrincipal CustomJwtUser principal
            ) {
        Profile profile = profileService.getProfileByUserId(principal.getId());
        return ResponseEntity.ok(profile);
    }


    @PostMapping(value = "/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAvatar(
            @AuthenticationPrincipal CustomJwtUser principal,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        long userId = principal.getId();
        String key = UUID.randomUUID() + Long.toString(userId);

        try (InputStream inputStream = file.getInputStream()) {
            CustomPutObjectDto dto = new CustomPutObjectDto(
                    avatarBucketName,
                    key,
                    inputStream,
                    file.getSize(),
                    file.getContentType()
            );

            profileService.getProfileAvatarByUserId(userId)
                    .ifPresent(avatar -> s3Service.delete(avatar.getAvatar()));

            String url =  s3Service.upload(dto);
            profileService.updateUserAvatarById(userId, url);

            return ResponseEntity.ok().body(url);
        }
    }
}
