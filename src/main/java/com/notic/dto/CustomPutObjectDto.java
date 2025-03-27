package com.notic.dto;

import java.io.InputStream;

public record CustomPutObjectDto(
        String bucket,
        String key,
        InputStream dataInputStream,
        long contentLength,
        String contentType
) {}
