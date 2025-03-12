package com.notic.service;

import com.notic.dto.CreateUserDto;
import com.notic.entity.User;
import com.notic.exception.UserAlreadyExistsException;
import com.notic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional(rollbackFor = UserAlreadyExistsException.class)
    public User createUser(CreateUserDto body) {
        if(userRepository.existsByEmail(body.email())) {
            throw new UserAlreadyExistsException("User with email " + body.email() + " already exists");
        }

        User user = new User(
                body.username(),
                body.email(),
                passwordEncoder.encode(body.password())
        );;

        return userRepository.save(user);
    }
}
