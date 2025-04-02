package com.notic.entity;


import com.notic.enums.FriendshipStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(
        name = "friendships",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_pair",
                        columnNames = {"sender_id", "receiver_id"}
                )
        }
)

@NoArgsConstructor
@Setter
@Getter
public class Friendship implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatusEnum status;

    public Friendship(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
        status = FriendshipStatusEnum.PENDING;
    }
}
