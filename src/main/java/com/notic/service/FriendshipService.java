package com.notic.service;

import com.notic.entity.Friendship;
import com.notic.entity.User;
import com.notic.enums.FriendshipStatusEnum;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.exception.FriendshipException;
import com.notic.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final UserService userService;

    private record FriendsPair(User sender, User receiver) {}

    @Transactional
    public void createFriendship(long senderId, long receiverId) {

        if(senderId == receiverId) {
            throw new FriendshipException("Sender and Receiver are the same");
        }

        boolean exists = friendshipRepository.existsByReceiver_IdAndSender_Id(receiverId, senderId);

        if(exists) {
            throw new EntityAlreadyExistsException("Friendship already exists");
        }

        FriendsPair pair = getFriendsPair(senderId, receiverId);

        Friendship friendship = new Friendship(pair.sender(), pair.receiver());

        friendshipRepository.save(friendship);
    }

    @Transactional
    public void acceptFriendship(long id, long receiverId) {
        Friendship friendship = friendshipRepository.findById(id)
                .orElseThrow(() -> new EntityDoesNotExistsException("Friendship not found"));

        if(friendship.getStatus() != FriendshipStatusEnum.PENDING || friendship.getReceiver().getId() != receiverId) {
            throw new FriendshipException("Friendship update error");
        }

        friendship.setStatus(FriendshipStatusEnum.ACCEPTED);
    }

    public void rejectFriendship(long id, long receiverId) {
        Friendship friendship = friendshipRepository.findById(id)
                .orElseThrow(() -> new EntityDoesNotExistsException("Friendship not found"));

        if(friendship.getStatus() != FriendshipStatusEnum.PENDING || friendship.getReceiver().getId() != receiverId) {
            throw new FriendshipException("Friendship update error");
        }

        friendship.setStatus(FriendshipStatusEnum.REJECTED);
    }

    private FriendsPair getFriendsPair(long senderId, long receiverId) {
        User sender = userService.getUserById(senderId)
                .orElseThrow(() -> new EntityDoesNotExistsException("Sender not found"));

        User receiver = userService.getUserById(receiverId)
                .orElseThrow(() -> new EntityDoesNotExistsException("Receiver not found"));

        return new FriendsPair(sender, receiver);
    }
}
