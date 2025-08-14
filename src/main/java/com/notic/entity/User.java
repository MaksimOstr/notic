package com.notic.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.notic.enums.AuthProviderEnum;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "users")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq")
    private long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 100)
    private String password;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    private AuthProviderEnum authProvider;

    @Column(nullable = false)
    private Boolean accountNonLocked = true;

    @Column(nullable = false)
    private Boolean enabled = false;

    @OneToMany(mappedBy = "author", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private List<Note> notes;

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE, optional = false, orphanRemoval = true)
    private Profile profile;

    @ManyToMany
    @JoinTable(
            name = "users_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    public User(String email, String password, Set<Role> roles) {
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.authProvider = AuthProviderEnum.LOCAL;

    }

    public User(String email, Set<Role> roles, AuthProviderEnum authProvider) {
        this.email = email;
        this.enabled = true;
        this.roles = roles;
        this.authProvider = authProvider;
    }
}
