package com.notic.entity;

import com.notic.enums.VerificationCodeScopeEnum;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "verification_codes_seq")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @Column(nullable = false, length = 8, unique = true)
    private int code;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VerificationCodeScopeEnum scope;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public VerificationCode(User user, int code, Instant expiresAt, VerificationCodeScopeEnum scope) {
        this.scope = scope;
        this.user = user;
        this.code = code;
        this.expiresAt = expiresAt;
    }
}
