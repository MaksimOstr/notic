package com.notic.service;

import com.notic.dto.CreateUserDto;
import com.notic.entity.Role;
import com.notic.entity.User;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.projection.JwtAuthUserProjection;
import com.notic.projection.UserCredentialsProjection;
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
        if(userRepository.existsByEmail(body.email())) {
            throw new EntityAlreadyExistsException("User already exists");
        }
        
        Role defaultRole = roleService.getDefaultRole();

        User user = new User(
                body.username(),
                body.email(),
                passwordEncoder.encode(body.password()),
                Set.of(defaultRole)
        );;

        return userRepository.save(user);
    }

    public Optional<GetUserAvatarProjection> getUserAvatarById(long id) {
        return userRepository.getUserAvatarUrlById(id);
    }

    public boolean isUserExistsById(long id) {
        return userRepository.existsById(id);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getUserById(long id) {
        return userRepository.findById(id);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityDoesNotExistsException("User not found"));
    }

    public Optional<User> getUserByEmailWithRoles(String email) {
        return userRepository.findByEmailWithRoles(email);
    }

    public Optional<UserCredentialsProjection> getUserForAuthByEmail(String email) {
        return userRepository.findUserForAuthByEmail(email);
    }

    public Optional<JwtAuthUserProjection> getUserForJwtAuth(long id) {
        return userRepository.findUserForJwtAuthById(id);

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
