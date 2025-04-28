package com.notic.service;

import com.notic.entity.Friendship;
import com.notic.entity.User;
import com.notic.exception.FriendshipException;
import com.notic.projection.FriendshipProjection;
import com.notic.repository.FriendshipRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.notic.criteria.FriendshipCriteria.existsFriendshipCriteria;


@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final EntityManager entityManager;


    public void createFriendship(long userId1, long userId2) {
        try {
            User user1 = entityManager.getReference(User.class, userId1);
            User user2 = entityManager.getReference(User.class, userId2);

            Friendship friendship = new Friendship(user1, user2);

            friendshipRepository.save(friendship);
        } catch (DataIntegrityViolationException e) {
            log.error("Friendship was not created{}", e.getMessage());
            throw new FriendshipException("Friendship was not created");
        }
    }

    
    @Transactional(readOnly = true)
    public Page<FriendshipProjection> getFriendships(long userId, Pageable pageable) {
        return friendshipRepository.findFriendshipsByUserId(userId, pageable);
    }

    public boolean isFriendshipExistsByUserId(long userId1, long userId2) {
        return friendshipRepository.exists(existsFriendshipCriteria(userId1, userId2));
    }

    public void deleteFriendship(long friendshipId, long userId) {
        int delete = friendshipRepository.removeFriendship(friendshipId, userId);

        if (delete == 0) {
            throw new FriendshipException("Friendship was not found");
        }
    }
}
