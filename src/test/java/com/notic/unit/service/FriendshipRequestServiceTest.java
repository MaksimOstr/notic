package com.notic.unit.service;

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
import com.notic.service.FriendshipRequestService;
import com.notic.service.FriendshipService;
import com.notic.service.UserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class FriendshipRequestServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private FriendshipRequestRepository friendshipRequestRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private UserService userService;

    @Captor
    private ArgumentCaptor<FriendshipRequestAcceptEvent> requestAcceptEventCaptor;

    @InjectMocks
    private FriendshipRequestService friendshipRequestService;


    @Nested
    class CreateFriendshipRequest {
        private final long senderId = 1L;
        private final long receiverId = 2L;

        @Test
        void shouldThrowErrorSenderAndReceiverAreSame() {
            assertThrows(FriendshipException.class, () ->
                    friendshipRequestService.createRequest(senderId, senderId)
            );

            verifyNoInteractions(friendshipRequestRepository, friendshipService, userService, eventPublisher);
        }

        @Test
        void shouldThrowErrorRequestExists() {
            when(friendshipRequestRepository.exists(Mockito.<Specification<FriendshipRequest>>any())).thenReturn(true);

            assertThrows(EntityAlreadyExistsException.class, () ->
                    friendshipRequestService.createRequest(senderId, receiverId)
            );

            verify(friendshipRequestRepository).exists(Mockito.<Specification<FriendshipRequest>>any());
            verifyNoInteractions(friendshipService, userService, eventPublisher);
        }

        @Test
        void shouldThrowErrorFriendshipExists() {
            when(friendshipRequestRepository.exists(Mockito.<Specification<FriendshipRequest>>any())).thenReturn(false);
            when(friendshipService.isFriendshipExistsByUserId(anyLong(), anyLong())).thenReturn(true);

            assertThrows(EntityAlreadyExistsException.class, () ->
                    friendshipRequestService.createRequest(senderId, receiverId)
            );

            verify(friendshipRequestRepository).exists(Mockito.<Specification<FriendshipRequest>>any());
            verify(friendshipService).isFriendshipExistsByUserId(senderId, receiverId);
            verifyNoMoreInteractions(friendshipRequestRepository);
            verifyNoInteractions(userService, eventPublisher);
        }

        @Test
        void shouldCreateFriendshipRequest() {
            User receiver = new User();
            User sender = new User("test", "pass", Set.of());
            String senderUsername = "test";
            String senderAvatar = "url";
            Profile senderProfile = new Profile(senderUsername, senderAvatar, sender);
            sender.setProfile(senderProfile);

            sender.setId(senderId);
            receiver.setId(receiverId);

            ArgumentCaptor<FriendshipRequest> captor = ArgumentCaptor.forClass(FriendshipRequest.class);
            ArgumentCaptor<FriendshipRequestCreatedEvent> eventCaptor = ArgumentCaptor.forClass(FriendshipRequestCreatedEvent.class);

            when(entityManager.getReference(User.class, receiverId)).thenReturn(receiver);
            when(friendshipRequestRepository.exists(Mockito.<Specification<FriendshipRequest>>any())).thenReturn(false);
            when(friendshipService.isFriendshipExistsByUserId(anyLong(), anyLong())).thenReturn(false);
            when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
            when(userService.isUserExistsById(receiverId)).thenReturn(true);

            friendshipRequestService.createRequest(senderId, receiverId);

            verify(friendshipRequestRepository).save(captor.capture());
            verify(friendshipRequestRepository).exists(Mockito.<Specification<FriendshipRequest>>any());
            verify(friendshipService).isFriendshipExistsByUserId(senderId, receiverId);
            verify(userService).getUserById(senderId);
            verify(userService).isUserExistsById(receiverId);
            verify(friendshipRequestRepository).save(any(FriendshipRequest.class));
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            FriendshipRequestCreatedEvent createdEvent = eventCaptor.getValue();
            Profile resultSenderProfile = sender.getProfile();

            assertEquals(senderId, captor.getValue().getSender().getId());
            assertEquals(receiverId, captor.getValue().getReceiver().getId());
            assertEquals(Long.toString(receiverId), createdEvent.receiverId());
            assertEquals(resultSenderProfile.getUsername(), createdEvent.username());
            assertEquals(resultSenderProfile.getAvatar(), createdEvent.avatarUrl());
        }
    }

    @Test
    void getAllFriendshipRequests() {
        final long receiverId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<FriendshipRequestProjection> resultPage = new PageImpl<>(List.of(), pageable, 1);

        when(friendshipRequestRepository.getAllFriendshipRequests(anyLong(), any(Pageable.class))).thenReturn(resultPage);

        Page<FriendshipRequestProjection> result = friendshipRequestService.getAllFriendshipRequests(receiverId, pageable);

        verify(friendshipRequestRepository).getAllFriendshipRequests(receiverId, pageable);

        assertNotNull(result);
        assertEquals(resultPage, result);
    }

    @Nested
    class AcceptFriendshipRequest {
        private final long senderId = 1L;
        private final long requestId = 2L;
        private final long receiverId = 3L;
        private final String receiverUsername = "bob";
        private final String receiverAvatar = "url";

        private final User sender = new User("test", "pass", Set.of());
        private final User receiver = new User("test", "pass", Set.of());
        private final Profile receiverProfile = new Profile(receiverUsername,receiverAvatar, receiver);

        @BeforeEach
        void beforeEach() {
            sender.setId(senderId);
            receiver.setId(receiverId);
            receiver.setProfile(receiverProfile);
        }


        @Test
        void shouldThrowRequestIsNotFound() {
            when(friendshipRequestRepository.findById(requestId, FriendshipAcceptRequestProjection.class)).thenReturn(Optional.empty());

            assertThrows(EntityDoesNotExistsException.class, () -> friendshipRequestService.acceptFriendshipRequest(requestId, receiverId));

            verify(friendshipRequestRepository).findById(requestId, FriendshipAcceptRequestProjection.class);
            verifyNoInteractions(friendshipService, eventPublisher);
            verifyNoMoreInteractions(friendshipRequestRepository);
        }

        @Test
        void shouldThrowIfUserIsNotReceiver() {
            long notReceiverId = 100L;
            FriendshipAcceptRequestProjection projection = mock(FriendshipAcceptRequestProjection.class);

            when(projection.getReceiverId()).thenReturn(receiverId);

            when(friendshipRequestRepository.findById(requestId, FriendshipAcceptRequestProjection.class)).thenReturn(Optional.of(projection));

            assertThrows(FriendshipException.class, () -> friendshipRequestService.acceptFriendshipRequest(requestId, notReceiverId));

            verify(friendshipRequestRepository).findById(requestId, FriendshipAcceptRequestProjection.class);
            verifyNoInteractions(friendshipService, eventPublisher);
            verifyNoMoreInteractions(friendshipRequestRepository);
        }

        @Test
        void shouldCreateFriendshipRequest() {
            FriendshipAcceptRequestProjection projection = mock(FriendshipAcceptRequestProjection.class);

            when(projection.getSenderId()).thenReturn(senderId);
            when(projection.getReceiverId()).thenReturn(receiverId);
            when(projection.getId()).thenReturn(requestId);
            when(projection.getReceiverAvatar()).thenReturn(receiverAvatar);
            when(projection.getReceiverUsername()).thenReturn(receiverUsername);

            when(friendshipRequestRepository.findById(requestId, FriendshipAcceptRequestProjection.class)).thenReturn(Optional.of(projection));
            doNothing().when(friendshipService).createFriendship(anyLong(), anyLong());
            doNothing().when(friendshipRequestRepository).deleteById(anyLong());

            friendshipRequestService.acceptFriendshipRequest(requestId, receiverId);

            verify(friendshipRequestRepository).findById(requestId, FriendshipAcceptRequestProjection.class);
            verify(friendshipService).createFriendship(senderId, receiverId);
            verify(friendshipRequestRepository).deleteById(requestId);
            verify(eventPublisher).publishEvent(requestAcceptEventCaptor.capture());

            FriendshipRequestAcceptEvent capturedEvent = requestAcceptEventCaptor.getValue();
            Profile resultReceiverProfile = receiver.getProfile();


            assertEquals(Long.toString(sender.getId()), capturedEvent.senderId());
            assertEquals(resultReceiverProfile.getUsername(), capturedEvent.username());
            assertEquals(resultReceiverProfile.getAvatar(), capturedEvent.avatarUrl());
        }
    }

    @Test
    void rejectFriendshipRequest() {
        final long requestId = 2L;
        final long receiverId = 3L;

        when(friendshipRequestRepository.rejectFriendshipRequest(anyLong(), anyLong())).thenReturn(0);


        assertThrows(FriendshipException.class, () -> friendshipRequestService.rejectFriendshipRequest(requestId, receiverId));

        verify(friendshipRequestRepository).rejectFriendshipRequest(requestId, receiverId);
    }

}
