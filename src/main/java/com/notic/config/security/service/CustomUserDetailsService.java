package com.notic.config.security.service;

import com.notic.config.security.model.CustomUserDetails;
import com.notic.entity.User;
import com.notic.enums.AuthProviderEnum;
import com.notic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.getUserByEmailWithRoles(email)
                .orElseThrow(() -> new UsernameNotFoundException("Authentication failed"));

        if(user.getAuthProvider() != AuthProviderEnum.LOCAL) {
            throw new UsernameNotFoundException("Account was created with another provider");
        }

        return new CustomUserDetails(user);
    }
}
