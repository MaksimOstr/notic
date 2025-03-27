package com.notic.service;


import com.notic.dto.CustomPutObjectDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    public String uploadUserAvatar(CustomPutObjectDto dto) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(dto.bucket())
                .key(dto.key())
                .acl(ObjectCannedACL.PUBLIC_READ)
                .contentType(dto.contentType())
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(
                    dto.dataInputStream(),
                    dto.contentLength())
            );

        return "https://" + dto.bucket() + ".s3.amazonaws.com/" + dto.key();
    }
}
