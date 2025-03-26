package com.notic.config.security.service;

import com.notic.mapper.UserMapper;
import com.notic.projection.UserCredentialsProjection;
import com.notic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserCredentialsProjection user = userService.getUserForAuthByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Authentication failed"));
        return userMapper.toCustomUserDetails(user);
    }
}
