package com.notic.service;

import com.notic.entity.Friendship;
import com.notic.entity.User;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import static com.notic.criteria.FriendshipCriteria.friendshipCriteria;


@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final UserService userService;

    private record FriendsPair(User user1, User user2) {}


    public void createFriendship(long userId1, long userId2) {
        FriendsPair pair = getFriendsPair(userId1, userId2);
        Friendship friendship = new Friendship(pair.user1(), pair.user2());

        friendshipRepository.save(friendship);
    }


    public boolean isFriendshipExistsByUserId(long userId1, long userId2) {
        return friendshipRepository.exists(friendshipCriteria(userId1, userId2));
    }

    private FriendsPair getFriendsPair(long userId1, long userId2) {
        User user1 = userService.getUserById(userId1)
                .orElseThrow(() -> new EntityDoesNotExistsException("Sender not found"));

        User user2 = userService.getUserById(userId2)
                .orElseThrow(() -> new EntityDoesNotExistsException("Receiver not found"));

        return new FriendsPair(user1, user2);
    }
}
