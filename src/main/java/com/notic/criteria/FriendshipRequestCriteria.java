package com.notic.criteria;

import com.notic.entity.FriendshipRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class FriendshipRequestCriteria {
    public static Specification<FriendshipRequest> existsFriendshipByUserId(long userId1, long userId2) {
        return (root, query, builder) -> {

            Predicate direction1 = builder.and(
                    builder.equal(root.get("sender").get("id"), userId1),
                    builder.equal(root.get("receiver").get("id"), userId2)
            );
            Predicate direction2 = builder.and(
                    builder.equal(root.get("sender").get("id"), userId2),
                    builder.equal(root.get("receiver").get("id"), userId1)
            );

            return builder.or(direction1, direction2);
        };
    }
}
