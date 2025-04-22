package com.notic.service;

import com.notic.dto.CreateUserDto;
import com.notic.entity.User;
import com.notic.enums.AuthProviderEnum;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.projection.GetUserAvatarProjection;
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


    @Transactional(rollbackFor = EntityAlreadyExistsException.class)
    public User createUser(CreateUserDto body) {
        isUserExistsByEmail(body.email());

        User user = new User(
                body.username(),
                body.email(),
                passwordEncoder.encode(body.password()),
                Set.of(roleService.getDefaultRole())
        );;

        return userRepository.save(user);
    }

    public User createGoogleUser(CreateUserDto body) {
        Optional<User> optionalUser = getUserByEmailWithRoles(body.email());
        if(optionalUser.isEmpty()) {
            User user = new User(
                    body.username(),
                    body.email(),
                    Set.of(roleService.getDefaultRole()),
                    AuthProviderEnum.GOOGLE
            );

            return userRepository.save(user);
        }

        return optionalUser.get();
    }

    public Optional<GetUserAvatarProjection> getUserAvatarById(long id) {
        return userRepository.getUserAvatarUrlById(id);
    }

    public boolean isUserExistsById(long id) {
        return userRepository.existsById(id);
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

    public void updateUserAvatarById(long id, String avatarUrl) {
        int updated = userRepository.updateUserAvatarById(id, avatarUrl);
        if(updated == 0) {
            throw new EntityDoesNotExistsException("User not found");
        }
    }
}
