package com.notic.projection;

import com.notic.entity.Role;

import java.util.Set;

public interface UserCredentialsProjection {
    String getEmail();
    String getPassword();
    boolean isAccountNonLocked();
    Set<Role> getRoles();
}
