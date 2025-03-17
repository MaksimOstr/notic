package com.notic.service;

import com.notic.dto.CreateUserDto;
import com.notic.entity.Role;
import com.notic.entity.User;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public User getUserByEmailWithRoles(String email) {
        return userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
