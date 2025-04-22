package com.notic.unit.service;

import com.notic.entity.Friendship;
import com.notic.entity.User;
import com.notic.exception.FriendshipException;
import com.notic.projection.FriendshipProjection;
import com.notic.repository.FriendshipRepository;
import com.notic.service.FriendshipService;
import com.notic.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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
    private UserService userService;

    @InjectMocks
    private FriendshipService friendshipService;

    private final long userId1 = 1L;
    private final long userId2 = 2L;

    @Test
    void shouldCreateFriendship() {
        User user1 = new User("Ivan", "test", "pass", Set.of());
        User user2 = new User("Oleg", "test", "pass", Set.of());

        user1.setId(userId1);
        user2.setId(userId2);

        ArgumentCaptor<Friendship> captor = ArgumentCaptor.forClass(Friendship.class);

        when(userService.getUserById(userId1)).thenReturn(Optional.of(user1));
        when(userService.getUserById(userId2)).thenReturn(Optional.of(user2));

        friendshipService.createFriendship(userId1, userId2);

        verify(friendshipRepository).save(captor.capture());
        verify(userService).getUserById(userId1);
        verify(userService).getUserById(userId2);

        Friendship saved = captor.getValue();

        assertEquals(userId1, saved.getUser1().getId());
        assertEquals(userId2, saved.getUser2().getId());
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
