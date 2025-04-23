package com.notic.config.security.service;

import com.notic.config.security.model.CustomJwtUser;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class CustomJwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {


    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        String userId = source.getSubject();
        List<String> roles = source.getClaimAsStringList("roles");

        CustomJwtUser principal = new CustomJwtUser(
                Long.parseLong(userId)
        );

        return new UsernamePasswordAuthenticationToken(principal, null, covertToGrantedAuthorities(roles));
    }

    private Collection<? extends GrantedAuthority> covertToGrantedAuthorities(List<String> roles) {
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}
