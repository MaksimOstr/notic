package com.notic.criteria;

import com.notic.entity.Friendship;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class FriendshipCriteria {
    public static Specification<Friendship> friendshipCriteria(long userId1, long userId2) {
        return (root, query, builder) -> {

            Predicate direction1 = builder.and(
                    builder.equal(root.get("user1").get("id"), userId1),
                    builder.equal(root.get("user2").get("id"), userId2)
            );
            Predicate direction2 = builder.and(
                    builder.equal(root.get("user1").get("id"), userId2),
                    builder.equal(root.get("user2").get("id"), userId1)
            );

            return builder.or(direction1, direction2);
        };
    }
}
