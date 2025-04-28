package com.notic.service;

import com.notic.dto.CreateProfileDto;
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

    public Profile getProfileByUserId(long id) {
        return profileRepository.findProfileByUser_Id(id)
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



    private boolean isProfileExists(long id) {
        return profileRepository.existsByUser_Id(id);
    }

    public Optional<GetProfileAvatarProjection> getProfileAvatarByUserId(long id) {
        return profileRepository.getProfileAvatarByUserId(id);
    }

    public void updateUserAvatarById(long id, String avatarUrl) {
        int updated = profileRepository.updateProfileAvatarByUserId(id, avatarUrl);
        if(updated == 0) {
            throw new EntityDoesNotExistsException("User not found");
        }
    }
}
