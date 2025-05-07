package com.notic.service;

import com.notic.dto.CreateProfileDto;
import com.notic.dto.request.UpdateProfileDto;
import com.notic.entity.Profile;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.projection.GetProfileAvatarProjection;
import com.notic.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

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

    public Optional<GetProfileAvatarProjection> getProfileAvatarByUserId(long userId) {
        return profileRepository.getProfileAvatarByUserId(userId);
    }

    @Transactional
    public Profile updateProfile(long userId, UpdateProfileDto dto) {
        Profile profile = getProfileByUserId(userId);

        profile.setUsername(dto.username());

        return profile;
    };

    public void updateUserAvatarById(long userId, String avatarUrl) {
        int updated = profileRepository.updateProfileAvatarByUserId(userId, avatarUrl);
        if(updated == 0) {
            throw new EntityDoesNotExistsException("User not found");
        }
    }

    private boolean isProfileExists(long userId) {
        return profileRepository.existsByUser_Id(userId);
    }
}
