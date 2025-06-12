package com.notic.projection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.notic.enums.AuthProviderEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public record UserWithRolesProjection(
        Long id,
        String email,
        String authProvider,
        String password,
        boolean accountNonLocked,
        boolean enabled,
        String roleNames
) {

    @JsonIgnore
    public Set<String> getRoleNamesSet() {
        return roleNames != null
                ? Arrays.stream(roleNames.split(",")).collect(Collectors.toSet())
                : Collections.emptySet();
    }

    @JsonIgnore
    public AuthProviderEnum getAuthProviderEnum() {
        return AuthProviderEnum.fromString(authProvider);
    }
}