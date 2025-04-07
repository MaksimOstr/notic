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
        name = "friendship_requests",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_pair",
                        columnNames = {"sender_id", "receiver_id"}
                )
        }
)
@NoArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class FriendshipRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User receiver;

    @CreatedDate
    private Instant createdAt;

    public FriendshipRequest(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }
}
