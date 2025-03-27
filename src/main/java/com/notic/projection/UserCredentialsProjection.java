package com.notic.projection;

import com.notic.entity.Role;
import java.util.Set;

public interface UserCredentialsProjection {
    long getId();
    String getEmail();
    String getPassword();
    boolean isAccountNonLocked();
    boolean isEnabled();
    Set<String> getRoleNames();
}
