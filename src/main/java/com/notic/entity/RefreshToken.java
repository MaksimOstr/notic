package com.notic.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;


@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@NamedEntityGraph(
        name = "RefreshToken.withUserAndRoles",
        attributeNodes = @NamedAttributeNode(value = "user", subgraph = "userRoles"),
        subgraphs = {
                @NamedSubgraph(
                        name = "userRoles",
                        attributeNodes = {
                                @NamedAttributeNode("roles"),
                                @NamedAttributeNode("profile")
                        }
                )
        }
)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @Column(unique = true, nullable = false)
    private String token;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public RefreshToken(String token, User user, Instant expiresAt) {
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
    }
}
