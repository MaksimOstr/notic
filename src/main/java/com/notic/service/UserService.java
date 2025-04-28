package com.notic.service;

import com.notic.dto.CreateLocalUserDto;
import com.notic.dto.CreateProfileDto;
import com.notic.dto.CreateProviderUserDto;
import com.notic.dto.UserWithProfileDto;
import com.notic.entity.Profile;
import com.notic.entity.User;
import com.notic.enums.AuthProviderEnum;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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


    @Transactional
    public UserWithProfileDto createUser(CreateLocalUserDto dto) {
        isUserExistsByEmail(dto.email());

        User user = new User(
                dto.email(),
                passwordEncoder.encode(dto.password()),
                Set.of(roleService.getDefaultRole())
        );;

        User createdUser = saveUser(user);

        Profile createdProfile = createUserProfile(createdUser, dto.username(), null);

        return new UserWithProfileDto(createdUser, createdProfile);
    }

    @Transactional
    public User createProviderUser(CreateProviderUserDto dto) {
        return userRepository.findByEmail(dto.email())
                .orElseGet(() -> {
                    User newUser = new User(
                            dto.email(),
                            Set.of(roleService.getDefaultRole()),
                            AuthProviderEnum.GOOGLE
                    );
                    User user = saveUser(newUser);

                    createUserProfile(user, dto.username(), dto.avatar());
                    return user;
                });
    }

    public boolean isUserExistsById(long id) {
        return userRepository.existsById(id);
    }

    public void markUserAsVerified(long id) {
        int updated = userRepository.updateEnabledStatusById(id, true);
        if(updated == 0) {
            throw new EntityDoesNotExistsException("User not found");
        }
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

    private User saveUser(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new EntityAlreadyExistsException("User with such email already exists: " + user.getEmail());
        }
    }

    private Profile createUserProfile(User user, String username, String avatar) {
        return profileService.createProfile(
                new CreateProfileDto(username, avatar, user)
        );
    }
}
