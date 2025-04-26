package com.notic.dto.response;

import com.notic.dto.UserWithProfileDto;
import lombok.Getter;

@Getter
public class SignUpResponseDto {
    private final long userId;
    private final String username;
    private final String email;
    private final String avatarUrl;

    public SignUpResponseDto(UserWithProfileDto dto) {
        this.userId = dto.user().getId();
        this.username = dto.profile().getUsername();
        this.email = dto.user().getEmail();
        this.avatarUrl = dto.profile().getAvatar();
    }
}
