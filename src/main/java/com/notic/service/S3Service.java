package com.notic.service;

import com.notic.dto.CustomPutObjectDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3AsyncClient s3AsyncClient;

    public CompletableFuture<String> upload(CustomPutObjectDto dto) {
        try {
            MultipartFile file = dto.file();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(dto.bucket())
                    .key(dto.key())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .contentType(file.getContentType())
                    .build();

            return s3AsyncClient.putObject(
                    request,
                    AsyncRequestBody.fromBytes(file.getBytes())
            ).thenApply(_ -> generateUrl(dto.bucket(), dto.key()));
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }


    public void delete(String itemUrl) {
        try {
            URI uri = new URI(itemUrl);
            String host = uri.getHost();
            String bucket = host.split("\\.")[0];
            String key = uri.getPath().substring(1);
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3AsyncClient.deleteObject(deleteObjectRequest);
        } catch (URISyntaxException e) {
            log.warn(e.getMessage());
        }

    }

    private String generateUrl(String bucket, String key) {
        return s3AsyncClient.utilities().getUrl(b -> b.bucket(bucket).key(key)).toString();
    }
}
