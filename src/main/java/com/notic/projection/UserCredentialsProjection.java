package com.notic.projection;

import com.notic.enums.AuthProviderEnum;
import java.util.Set;

public interface UserCredentialsProjection {
    long getId();
    String getEmail();
    String getPassword();
    boolean isAccountNonLocked();
    boolean isEnabled();
    AuthProviderEnum getAuthProvider();
    Set<String> getRoleNames();
}
