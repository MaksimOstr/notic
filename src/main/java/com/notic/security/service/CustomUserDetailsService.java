package com.notic.security.service;

import com.notic.entity.User;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.mapper.UserMapper;
import com.notic.projection.UserCredentialsProjection;
import com.notic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


//Remove throwing entity not found. Instead of it throw only UsernameNotFoundException!!!!!
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            UserCredentialsProjection user = userService.getUserForAuth(email);
            return userMapper.toCustomUserDetails(user);
        } catch (EntityDoesNotExistsException e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        } catch (Exception e) {
            throw new UsernameNotFoundException("Authentication failed", e);
        }
    }
}
