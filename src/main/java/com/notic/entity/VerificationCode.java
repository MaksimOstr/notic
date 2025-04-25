package com.notic.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "verification_codes")
@NoArgsConstructor
@Getter
@Setter
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @Column(nullable = false, length = 8, unique = true)
    private long code;

    @Column(nullable = false)
    private Instant expiresAt;

    public VerificationCode(User user, long code, Instant expiresAt) {
        this.user = user;
        this.code = code;
        this.expiresAt = expiresAt;
    }
}
