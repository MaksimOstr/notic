package com.notic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.notic.dto.CreateProfileDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    private String avatar;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public Profile(CreateProfileDto dto) {
        this.username = dto.username();
        this.avatar = dto.avatar();
        this.user = dto.user();
    }

    public Profile(String username, String avatar, User user) {
        this.username = username;
        this.avatar = avatar;
        this.user = user;
    }
}
