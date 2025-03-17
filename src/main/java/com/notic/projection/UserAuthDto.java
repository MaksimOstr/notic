package com.notic.projection;

import lombok.Getter;

import java.util.Set;

@Getter
public class UserAuthDto {
    private String email;
    private String password;
    private boolean accountNonLocked;
    private Set<String> roles;

    public UserAuthDto(String email, String password, boolean accountNonLocked, Set<String> roles) {
        this.email = email;
        this.password = password;
        this.accountNonLocked = accountNonLocked;
        this.roles = roles;
    }

}
