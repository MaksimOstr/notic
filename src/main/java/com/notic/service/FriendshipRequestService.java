package com.notic.service;

import com.notic.entity.FriendshipRequest;
import com.notic.entity.Profile;
import com.notic.entity.User;
import com.notic.event.FriendshipRequestAcceptEvent;
import com.notic.event.FriendshipRequestCreatedEvent;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.exception.FriendshipException;
import com.notic.projection.FriendshipAcceptRequestProjection;
import com.notic.projection.FriendshipRequestProjection;
import com.notic.repository.FriendshipRequestRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;

    static final String FRIENDSHIP_REQUEST_NOT_FOUND  = "Friendship request not found";
    static final String FRIENDSHIP_REQUEST_ALREADY_EXISTS = "Friendship request already exists";

    private record FriendshipRequestPair(User sender, User receiver) {}


    @Transactional
    public void createRequest(long senderId, long receiverId) {
        if(senderId == receiverId) {
            throw new FriendshipException("Sender and Receiver are the same");
        }
        ensureNoExistingRequestOrFriendship(senderId, receiverId);
        FriendshipRequestPair userPair = getSenderAndReceiver(senderId, receiverId);
        try {
            FriendshipRequest friendshipRequest = new FriendshipRequest(userPair.sender(), userPair.receiver());
            Profile senderProfile = userPair.sender().getProfile();
            friendshipRequestRepository.save(friendshipRequest);
            publishRequestCreatedEvent(receiverId, senderProfile);
        } catch (DataIntegrityViolationException e) {
            throw new FriendshipException(FRIENDSHIP_REQUEST_ALREADY_EXISTS);
        }
    }


    @Transactional(readOnly = true)
    public Page<FriendshipRequestProjection> getAllFriendshipRequests(long receiverId, Pageable pageable) {
        return friendshipRequestRepository.getAllFriendshipRequests(receiverId, pageable);
    }


    @Transactional
    public void acceptFriendshipRequest(long requestId, long receiverId) {
        FriendshipAcceptRequestProjection request = friendshipRequestRepository.findById(requestId, FriendshipAcceptRequestProjection.class)
                .orElseThrow(() -> new EntityDoesNotExistsException(FRIENDSHIP_REQUEST_NOT_FOUND));

        if(request.getReceiverId() != receiverId) {
            throw new FriendshipException("Friendship request accept error");
        }

        friendshipService.createFriendship(
                request.getSenderId(),
                receiverId
        );

        friendshipRequestRepository.deleteById(request.getId());
        publishAcceptEvent(request);
    }


    @Transactional
    public void rejectFriendshipRequest(long requestId, long receiverId) {
        int delete = friendshipRequestRepository.rejectFriendshipRequest(requestId, receiverId);

        if(delete == 0) {
            throw new FriendshipException(FRIENDSHIP_REQUEST_NOT_FOUND);
        }
    }


    private FriendshipRequestPair getSenderAndReceiver(long senderId, long receiverId) {
        User sender = userService.getUserById(senderId)
                .orElseThrow(() -> new EntityDoesNotExistsException("Friendship request sender not found"));

        boolean receiver = userService.isUserExistsById(receiverId);

        if(!receiver) {
            throw new EntityDoesNotExistsException("Friendship request receiver not found");
        }

        User receiverUser = entityManager.getReference(User.class, receiverId);

        return new FriendshipRequestPair(sender, receiverUser);
    }

    private void ensureNoExistingRequestOrFriendship(long senderId, long receiverId) {
        boolean existsRequest = friendshipRequestRepository.exists(existsFriendshipByUserId(senderId, receiverId));
        if(existsRequest) {
            throw new EntityAlreadyExistsException(FRIENDSHIP_REQUEST_ALREADY_EXISTS);
        }
        boolean existsFriendship = friendshipService.isFriendshipExistsByUserId(senderId, receiverId);
        if(existsFriendship) {
            throw new EntityAlreadyExistsException("Friendship already exists");
        }
    }

    private void publishRequestCreatedEvent(long receiverId, Profile senderProfile) {
        eventPublisher.publishEvent(new FriendshipRequestCreatedEvent(
                Long.toString(receiverId),
                senderProfile.getUsername(),
                senderProfile.getAvatar()
        ));
    }

    private void publishAcceptEvent(FriendshipAcceptRequestProjection request) {
        eventPublisher.publishEvent(new FriendshipRequestAcceptEvent(
                Long.toString(request.getSenderId()),
                request.getReceiverUsername(),
                request.getReceiverAvatar()
        ));
    }
}
