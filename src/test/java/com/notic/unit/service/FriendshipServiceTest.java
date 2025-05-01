package com.notic.unit.service;

import com.notic.entity.Friendship;
import com.notic.entity.User;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.FriendshipException;
import com.notic.projection.FriendshipProjection;
import com.notic.repository.FriendshipRepository;
import com.notic.service.FriendshipService;
import com.notic.service.UserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private FriendshipService friendshipService;

    @Captor
    private ArgumentCaptor<Friendship> friendshipCaptor;

    private final long userId1 = 1123L;
    private final long userId2 = 4232342L;

    @Nested
    class CreateFriendship {
        User user1 = new User();
        User user2 = new User();

        @BeforeEach
        void setUp() {
            user1.setId(userId1);
            user2.setId(userId2);
        }

        @Test
        void shouldCreateFriendship() {
            when(entityManager.getReference(User.class, userId1)).thenReturn(user1);
            when(entityManager.getReference(User.class, userId2)).thenReturn(user2);

            friendshipService.createFriendship(userId1, userId2);

            verify(friendshipRepository).save(friendshipCaptor.capture());
            verify(entityManager).getReference(User.class, userId1);
            verify(entityManager).getReference(User.class, userId2);

            Friendship saved = friendshipCaptor.getValue();

            assertEquals(userId1, saved.getUser1().getId());
            assertEquals(userId2, saved.getUser2().getId());
        }


        @Test
        void shouldThrowDataIntegrityViolationException() {
            when(entityManager.getReference(User.class, userId1)).thenReturn(user1);
            when(entityManager.getReference(User.class, userId2)).thenReturn(user2);
            when(friendshipRepository.save(any(Friendship.class))).thenThrow(new DataIntegrityViolationException(""));

            assertThrows(FriendshipException.class, () -> friendshipService.createFriendship(userId1, userId2));

            verify(friendshipRepository).save(friendshipCaptor.capture());
            verify(entityManager).getReference(User.class, userId1);
            verify(entityManager).getReference(User.class, userId2);

            Friendship saved = friendshipCaptor.getValue();

            assertEquals(userId1, saved.getUser1().getId());
            assertEquals(userId2, saved.getUser2().getId());
        }
    }

    @Test
    void getFriendships() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<FriendshipProjection> resultPage = new PageImpl<>(List.of(), pageable, 1);

        when(friendshipRepository.findFriendshipsByUserId(userId1, pageable)).thenReturn(resultPage);

        Page<FriendshipProjection> result = friendshipService.getFriendships(userId1, pageable);

        verify(friendshipRepository).findFriendshipsByUserId(userId1, pageable);
        assertEquals(resultPage, result);
    }

    @Test
    void isFriendshipExistsByUserId() {
        when(friendshipRepository.exists(Mockito.<Specification<Friendship>>any())).thenReturn(true);

        boolean result = friendshipService.isFriendshipExistsByUserId(userId1, userId2);

        verify(friendshipRepository).exists(Mockito.<Specification<Friendship>>any());

        assertTrue(result);
    }

    @Test
    void deleteFriendshipShouldThrowException() {
        when(friendshipRepository.removeFriendship(anyLong(), anyLong())).thenReturn(0);

        assertThrows(FriendshipException.class, () -> friendshipService.deleteFriendship(userId1, userId2));

        verify(friendshipRepository).removeFriendship(userId1, userId2);
    }
}
