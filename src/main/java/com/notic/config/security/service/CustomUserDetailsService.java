package com.notic.config.security.service;

import com.notic.config.security.model.CustomUserDetails;
import com.notic.entity.User;
import com.notic.enums.AuthProviderEnum;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.projection.UserWithRolesProjection;
import com.notic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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
       try {
           UserWithRolesProjection user = userService.getUserByEmailWithRoles(email);

           if(user.getAuthProviderEnum() != AuthProviderEnum.LOCAL) {
               throw new UsernameNotFoundException("Account was created with another provider");
           }

           return new CustomUserDetails(user);
       } catch (EntityDoesNotExistsException e) {
           throw new UsernameNotFoundException(e.getMessage());
       }
    }
}
