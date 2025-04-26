package com.notic.service;

import com.notic.dto.CreateProfileDto;
import com.notic.dto.CreateUserDto;
import com.notic.dto.UserWithProfileDto;
import com.notic.entity.Profile;
import com.notic.entity.User;
import com.notic.enums.AuthProviderEnum;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final ProfileService profileService;


    @Transactional(rollbackFor = EntityAlreadyExistsException.class)
    public UserWithProfileDto createUser(CreateUserDto userDto, CreateProfileDto profileDto) {
        isUserExistsByEmail(userDto.email());

        User user = new User(
                userDto.email(),
                passwordEncoder.encode(userDto.password()),
                Set.of(roleService.getDefaultRole())
        );;

        User createdUser = userRepository.save(user);
        Profile createdProfile = profileService.createProfile(profileDto);

        return new UserWithProfileDto(createdUser, createdProfile);
    }

    public User createGoogleUser(CreateUserDto body) {
        Optional<User> optionalUser = getUserByEmailWithRoles(body.email());
        if(optionalUser.isEmpty()) {
            User user = new User(
                    body.email(),
                    Set.of(roleService.getDefaultRole()),
                    AuthProviderEnum.GOOGLE
            );

            return userRepository.save(user);
        }

        return optionalUser.get();
    }

    public boolean isUserExistsById(long id) {
        return userRepository.existsById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserById(long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmailWithRoles(String email) {
        return userRepository.findByEmailWithRoles(email);
    }

    private void isUserExistsByEmail(String email) {
        if(userRepository.existsByEmail(email)) {
            throw new EntityAlreadyExistsException("User already exists");
        }
    }

    public void markUserAsVerified(long id) {
        int updated = userRepository.updateEnabledStatusById(id, true);
        if(updated == 0) {
            throw new EntityDoesNotExistsException("User not found");
        }
    }
}
