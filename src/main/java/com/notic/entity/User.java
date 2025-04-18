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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @CreatedDate
    private Instant createdAt;

    private String avatar;

    @Enumerated(EnumType.STRING)
    private AuthProviderEnum authProvider;

    @Column(nullable = false)
    private Boolean accountNonLocked;

    @Column(nullable = false)
    private Boolean enabled;

    @OneToMany(mappedBy = "author", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private List<Note> notes;

    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    public User(String username, String email, String password, Set<Role> roles) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.authProvider = AuthProviderEnum.LOCAL;

    }

    public User(String username, String email, Set<Role> roles, AuthProviderEnum authProvider) {
        this.username = username;
        this.email = email;
        this.enabled = true;
        this.roles = roles;
        this.authProvider = authProvider;
    }

    @PrePersist
    void onCreate() {
        if (accountNonLocked == null) {
            accountNonLocked = true;
        }

        if(enabled == null) {
            enabled = true;
        }
    }
}
