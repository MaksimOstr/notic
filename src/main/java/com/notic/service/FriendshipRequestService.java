package com.notic.service;


import com.notic.entity.FriendshipRequest;
import com.notic.entity.User;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.exception.FriendshipException;
import com.notic.projection.FriendshipRequestProjection;
import com.notic.repository.FriendshipRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.notic.criteria.FriendshipRequestCriteria.*;


@Service
@RequiredArgsConstructor
public class FriendshipRequestService {

    private final FriendshipRequestRepository friendshipRequestRepository;
    private final FriendshipService friendshipService;
    private final UserService userService;

    private record FriendsPair(User sender, User receiver) {}

    @Transactional
    public void createRequest(long senderId, long receiverId) {
        if(senderId == receiverId) {
            throw new FriendshipException("Sender and Receiver are the same");
        }

        boolean existsRequest = friendshipRequestRepository.exists(existsFriendshipByUserId(senderId, receiverId));

        if(existsRequest) {
            throw new EntityAlreadyExistsException("Friendship request already exists");
        }

        boolean existsFriendship = friendshipService.isFriendshipExistsByUserId(senderId, receiverId);

        if(existsFriendship) {
            throw new EntityAlreadyExistsException("Friendship already exists");
        }

        FriendsPair pair = getFriendsPair(senderId, receiverId);
        FriendshipRequest friendshipRequest = new FriendshipRequest(pair.sender(), pair.receiver());

        friendshipRequestRepository.save(friendshipRequest);
    }

    @Transactional(readOnly = true)
    public Page<FriendshipRequestProjection> getAllFriendshipRequests(long receiverId, Pageable pageable) {
        return friendshipRequestRepository.getAllFriendshipRequests(receiverId, pageable);
    }

    @Transactional
    public void acceptFriendshipRequest(long requestId, long receiverId) {
        FriendshipRequest request = friendshipRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityDoesNotExistsException("Friendship request not found"));

        if(request.getReceiver().getId() != receiverId) {
            throw new FriendshipException("Friendship update error");
        }

        friendshipService.createFriendship(
                request.getSender().getId(),
                receiverId
        );
        friendshipRequestRepository.deleteById(request.getId());
    }

    @Transactional
    public void rejectFriendshipRequest(long requestId, long receiverId) {
        int delete = friendshipRequestRepository.rejectFriendshipRequest(requestId, receiverId);

        if(delete == 0) {
            throw new FriendshipException("Friendship error");
        }
    }


    private FriendsPair getFriendsPair(long senderId, long receiverId) {
        User sender = userService.getUserById(senderId)
                .orElseThrow(() -> new EntityDoesNotExistsException("Sender not found"));

        User receiver = userService.getUserById(receiverId)
                .orElseThrow(() -> new EntityDoesNotExistsException("Receiver not found"));

        return new FriendsPair(sender, receiver);
    }
}
