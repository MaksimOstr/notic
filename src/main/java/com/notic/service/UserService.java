package com.notic.service;

import com.notic.dto.CreateUserDto;
import com.notic.entity.Role;
import com.notic.entity.User;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
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
            throw new EntityAlreadyExistsException("User with email " + body.email() + " already exists");
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

}
