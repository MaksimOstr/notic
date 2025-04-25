package com.notic.controller;

import com.notic.config.security.model.CustomJwtUser;
import com.notic.dto.CustomPutObjectDto;
import com.notic.entity.Profile;
import com.notic.service.ProfileService;
import com.notic.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @GetMapping("/personal")
    public ResponseEntity<Profile> getPersonalProfile(
            @AuthenticationPrincipal CustomJwtUser principal
            ) {
        Profile profile = profileService.getProfileByUserId(principal.getId());
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/upload-avatar")
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
