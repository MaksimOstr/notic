package com.notic.security.service;

import com.notic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println(email);
        return userService.getUserByEmailWithRoles(email)
                .map((user) -> new User(user.getEmail(), user.getPassword(), true, true, true, user.getAccountNonLocked(), user.getRoles()))
                .orElseThrow(() -> new UsernameNotFoundException("There is no such user"));
    }
}
