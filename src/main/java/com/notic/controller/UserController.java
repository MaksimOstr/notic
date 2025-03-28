package com.notic.controller;

import com.notic.config.security.model.CustomUserDetails;
import com.notic.dto.CustomPutObjectDto;
import com.notic.service.S3Service;
import com.notic.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        long userId = userDetails.getUserId();
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
