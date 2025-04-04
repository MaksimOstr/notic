package com.notic.controller;

import com.notic.dto.CustomPutObjectDto;
import com.notic.projection.JwtAuthUserProjection;
import com.notic.service.S3Service;
import com.notic.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final S3Service s3Service;

    @PostMapping("/upload-avatar")
    public ResponseEntity<String> uploadAvatar(
            @AuthenticationPrincipal JwtAuthUserProjection principal,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        long userId = principal.getId();
        String key = UUID.randomUUID() + Long.toString(userId);

        try (InputStream inputStream = file.getInputStream()) {
            CustomPutObjectDto dto = new CustomPutObjectDto(
                    "noticavatar",
                    key,
                    inputStream,
                    file.getSize(),
                    file.getContentType()
            );

            String url =  s3Service.uploadUserAvatar(dto);
            userService.updateUserAvatarById(userId, url);

            return ResponseEntity.ok().body(url);
        }
    }
}
