package com.notic.dto;

import org.springframework.web.multipart.MultipartFile;


public record CustomPutObjectDto(
        String bucket,
        String key,
        MultipartFile file
) {}
