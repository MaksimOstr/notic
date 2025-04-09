package com.notic.unit.service;

import com.notic.entity.FriendshipRequest;
import com.notic.entity.User;
import com.notic.event.FriendshipRequestAcceptEvent;
import com.notic.event.FriendshipRequestCreatedEvent;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.exception.FriendshipException;
import com.notic.projection.FriendshipRequestProjection;
import com.notic.repository.FriendshipRequestRepository;
import com.notic.service.FriendshipRequestService;
import com.notic.service.FriendshipService;
import com.notic.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    private FriendshipService friendshipService;

    @Mock
    private UserService userService;

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

            verify(friendshipRequestRepository, never()).exists(Mockito.<Specification<FriendshipRequest>>any());
            verify(friendshipService, never()).isFriendshipExistsByUserId(anyLong(), anyLong());
            verify(friendshipRequestRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any(FriendshipRequestCreatedEvent.class));
        }

        @Test
        void shouldThrowErrorRequestExists() {
            when(friendshipRequestRepository.exists(Mockito.<Specification<FriendshipRequest>>any())).thenReturn(true);

            assertThrows(EntityAlreadyExistsException.class, () ->
                    friendshipRequestService.createRequest(senderId, receiverId)
            );

            verify(friendshipRequestRepository, times(1)).exists(Mockito.<Specification<FriendshipRequest>>any());
            verify(friendshipService, never()).isFriendshipExistsByUserId(anyLong(), anyLong());
            verify(friendshipRequestRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any(FriendshipRequestCreatedEvent.class));
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
            verify(friendshipRequestRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any(FriendshipRequestCreatedEvent.class));
        }

        @Test
        void shouldCreateFriendshipRequest() {
            User sender = new User("Ivan", "test", "pass", Set.of());
            User receiver = new User("Oleg", "test", "pass", Set.of());

            sender.setId(senderId);
            receiver.setId(receiverId);

            ArgumentCaptor<FriendshipRequest> captor = ArgumentCaptor.forClass(FriendshipRequest.class);
            ArgumentCaptor<FriendshipRequestCreatedEvent> eventCaptor = ArgumentCaptor.forClass(FriendshipRequestCreatedEvent.class);

            when(friendshipRequestRepository.exists(Mockito.<Specification<FriendshipRequest>>any())).thenReturn(false);
            when(friendshipService.isFriendshipExistsByUserId(anyLong(), anyLong())).thenReturn(false);
            when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
            when(userService.getUserById(receiverId)).thenReturn(Optional.of(receiver));

            friendshipRequestService.createRequest(senderId, receiverId);

            verify(friendshipRequestRepository).save(captor.capture());
            verify(friendshipRequestRepository).exists(Mockito.<Specification<FriendshipRequest>>any());
            verify(friendshipService).isFriendshipExistsByUserId(senderId, receiverId);
            verify(friendshipRequestRepository).save(any(FriendshipRequest.class));
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            FriendshipRequestCreatedEvent createdEvent = eventCaptor.getValue();

            assertEquals(senderId, captor.getValue().getSender().getId());
            assertEquals(receiverId, captor.getValue().getReceiver().getId());
            assertEquals(Long.toString(receiverId), createdEvent.receiverId());
            assertEquals(sender.getUsername(), createdEvent.username());
            assertEquals(sender.getAvatar(), createdEvent.avatarUrl());
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

        private final User sender = new User("Ivan", "test", "pass", Set.of());
        private final User receiver = new User("Oleg", "test", "pass", Set.of());

        @BeforeEach
        void beforeEach() {
            sender.setId(senderId);
            receiver.setId(receiverId);
        }


        @Test
        void shouldThrowRequestIsNotFound() {
            when(friendshipRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(EntityDoesNotExistsException.class, () -> friendshipRequestService.acceptFriendshipRequest(requestId, receiverId));

            verify(friendshipRequestRepository).findById(requestId);
            verify(friendshipService, never()).createFriendship(anyLong(), anyLong());
            verify(friendshipRequestRepository, never()).deleteById(anyLong());
            verify(eventPublisher, never()).publishEvent(any(FriendshipRequestAcceptEvent.class));
        }

        @Test
        void shouldThrowIfUserIsNotReceiver() {
            long notReceiverId = 100L;

            when(friendshipRequestRepository.findById(anyLong())).thenReturn(Optional.of(new FriendshipRequest(sender, receiver)));

            assertThrows(FriendshipException.class, () -> friendshipRequestService.acceptFriendshipRequest(requestId, notReceiverId));

            verify(friendshipRequestRepository).findById(requestId);
            verify(friendshipService, never()).createFriendship(anyLong(), anyLong());
            verify(friendshipRequestRepository, never()).deleteById(anyLong());
            verify(eventPublisher, never()).publishEvent(any(FriendshipRequestAcceptEvent.class));
        }

        @Test
        void shouldCreateFriendshipRequest() {

            ArgumentCaptor<FriendshipRequestAcceptEvent> eventCaptor = ArgumentCaptor.forClass(FriendshipRequestAcceptEvent.class);

            FriendshipRequest friendshipRequest = new FriendshipRequest(sender, receiver);
            friendshipRequest.setId(requestId);

            when(friendshipRequestRepository.findById(anyLong())).thenReturn(Optional.of(friendshipRequest));
            doNothing().when(friendshipService).createFriendship(anyLong(), anyLong());
            doNothing().when(friendshipRequestRepository).deleteById(anyLong());

            friendshipRequestService.acceptFriendshipRequest(requestId, receiverId);

            verify(friendshipRequestRepository).findById(requestId);
            verify(friendshipService).createFriendship(senderId, receiverId);
            verify(friendshipRequestRepository).deleteById(requestId);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            FriendshipRequestAcceptEvent capturedEvent = eventCaptor.getValue();

            assertEquals(Long.toString(sender.getId()), capturedEvent.senderId());
            assertEquals(receiver.getUsername(), capturedEvent.username());
            assertEquals(receiver.getAvatar(), capturedEvent.avatarUrl());
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
