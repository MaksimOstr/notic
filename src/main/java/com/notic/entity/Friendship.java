package com.notic.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;


@Entity
@Table(
        name = "friendships",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_pair",
                        columnNames = {"user1_id", "user2_id"}
                )
        }
)
@NoArgsConstructor
@Setter
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user2;

    @CreatedDate
    @Column(name = "friendship_date", nullable = false)
    private Instant friendshipDate;

    public Friendship(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
    }
}
