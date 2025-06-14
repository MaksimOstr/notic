package com.notic.service;

import com.notic.dto.CreateProfileDto;
import com.notic.dto.CustomPutObjectDto;
import com.notic.dto.request.UpdateProfileDto;
import com.notic.entity.Profile;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final S3Service s3Service;
    private final CacheManager cacheManager;

    @Value("${AWS_AVATAR_BUCKET_NAME}")
    private String avatarBucketName;

    private static final String PROFILE_CACHE_NAME = "profiles";

    @Cacheable(cacheNames = PROFILE_CACHE_NAME, key = "#userId")
    public Profile getProfileByUserId(long userId) {
        return profileRepository.findProfileByUser_Id(userId)
                .orElseThrow(() -> new EntityDoesNotExistsException("Profile not found"));
    }

    @Transactional
    public Profile createProfile(CreateProfileDto dto) {
        long userId = dto.user().getId();

        if(isProfileExists(userId)) {
            throw new EntityAlreadyExistsException("Profile for user " + userId + " already exists");
        }

        Profile profile = new Profile(dto);

        return profileRepository.save(profile);
    }

    public Optional<String> getProfileAvatarByUserId(long userId) {
        return profileRepository.getProfileAvatarByUser_Id(userId);
    }

    @Transactional
    @CachePut(cacheNames = PROFILE_CACHE_NAME, key = "#userId")
    public Profile updateProfile(long userId, UpdateProfileDto dto) {
        Profile profile = getProfileByUserId(userId);

        profile.setUsername(dto.username());

        return profile;
    };

    @CacheEvict(cacheNames = PROFILE_CACHE_NAME, key = "#userId")
    public CompletableFuture<String> updateUserAvatarById(long userId, MultipartFile file) {
        getProfileAvatarByUserId(userId)
                .ifPresent(s3Service::delete);

        return uploadProfileAvatar(file, userId)
                .thenApply(url -> {
                    int updated = profileRepository.updateProfileAvatarByUserId(userId, url);
                    if(updated == 0) {
                        throw new EntityDoesNotExistsException("User not found");
                    }

                    return url;
                });


    }

    private CompletableFuture<String> uploadProfileAvatar(MultipartFile file, long userId) {
        String key = UUID.randomUUID().toString() + userId;
        return s3Service.upload(new CustomPutObjectDto(
                avatarBucketName,
                key,
                file
        ));
    }

    private boolean isProfileExists(long userId) {
        return profileRepository.existsByUser_Id(userId);
    }
}
