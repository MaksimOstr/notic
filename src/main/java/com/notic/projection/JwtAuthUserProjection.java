package com.notic.projection;


public interface JwtAuthUserProjection {
    long getId();
    boolean isAccountNonLocked();
}
