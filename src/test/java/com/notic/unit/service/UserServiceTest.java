package com.notic.unit.service;

import com.notic.dto.CreateUserDto;
import com.notic.entity.Role;
import com.notic.entity.User;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.projection.GetUserAvatarProjection;
import com.notic.repository.UserRepository;
import com.notic.service.RoleService;
import com.notic.service.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;


    @Nested
    class CreateUser {

        private final CreateUserDto createUserDto = new CreateUserDto("test@gmail.com", "test", "12121212");

        @Test
        void successfullyCreateUser() {

            CreateUserDto createUserDto = new CreateUserDto("test@gmail.com", "test", "12121212");
            Role userRole = new Role("ROLE_USER");
            String encodedPassword = "encoded";
            User createdUser = new User(createUserDto.username(), createUserDto.email(), encodedPassword, Set.of(userRole));

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleService.getDefaultRole()).thenReturn(userRole);
            when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
            when(userRepository.save(any(User.class))).thenReturn(createdUser);

            User result = userService.createUser(createUserDto);


            verify(roleService).getDefaultRole();
            verify(passwordEncoder).encode(createUserDto.password());
            verify(userRepository).save(createdUser);
            verify(userRepository).existsByEmail(createUserDto.email());



            assertNotNull(result);
            assertEquals(createUserDto.username(), result.getUsername());
            assertEquals(createUserDto.email(), result.getEmail());
            assertNotEquals(createUserDto.password(), result.getPassword());
            assertEquals(encodedPassword, result.getPassword());
        }

        @Test
        void userAlreadyExists() {
            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            Exception result = assertThrows(EntityAlreadyExistsException.class, () -> userService.createUser(createUserDto));
            assertEquals("User already exists", result.getMessage());

            verify(userRepository).existsByEmail(createUserDto.email());
            verify(roleService, never()).getDefaultRole();
            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(anyString());
        }
    }






    @Nested
    class GetUserByEmailWithRoles {
        private final String email = "test@gmail.com";
        private final String password = "12121212";
        private final User user = new User("test", email, password, Set.of(new Role("ROLE_USER")));

        @Test
        void userDoesNotExist() {
            when(userRepository.findByEmailWithRoles(anyString())).thenReturn(Optional.empty());

            Optional<User> result = userService.getUserByEmailWithRoles(email);

            verify(userRepository).findByEmailWithRoles(email);

            assertFalse(result.isPresent());
        }

        @Test
        void userExists() {
            when(userRepository.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));

            Optional<User> result = userService.getUserByEmailWithRoles(email);

            assertTrue(result.isPresent());
            assertNotNull(result.get());
            verify(userRepository).findByEmailWithRoles(email);
            assertEquals(user, result.get());
        }
    }


    @Nested
    class MarkUserAsVerified {
        private final long userId = 1L;

        @Test
        void shouldThrowExceptionWhenUserDoesNotExist() {
            when(userRepository.updateEnabledStatusById(anyLong(), anyBoolean())).thenReturn(0);

            assertThrows(EntityDoesNotExistsException.class, () -> userService.markUserAsVerified(userId));

            verify(userRepository).updateEnabledStatusById(userId, true);
        }

        @Test
        void shouldMarkUserAsVerified() {
            when(userRepository.updateEnabledStatusById(anyLong(), anyBoolean())).thenReturn(1);

            userService.markUserAsVerified(userId);

            verify(userRepository).updateEnabledStatusById(userId, true);
        }
    }

    @Test
    void getUserAvatarById() {
        long userId = 1L;

        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();

        GetUserAvatarProjection projection = factory.createProjection(
                GetUserAvatarProjection.class,
                new Object[]{"avatarUrl"}
        );

        when(userRepository.getUserAvatarUrlById(anyLong())).thenReturn(Optional.of(projection));

        Optional<GetUserAvatarProjection> result = userService.getUserAvatarById(userId);

        verify(userRepository).getUserAvatarUrlById(userId);

        assertTrue(result.isPresent());
        assertEquals(projection, result.get());
    }

    @Test
    void isUserExistsById() {
        long userId = 1L;

        when(userRepository.existsById(anyLong())).thenReturn(true);

        boolean result = userService.isUserExistsById(userId);

        verify(userRepository).existsById(userId);

        assertTrue(result);
    }

    @Test
    void getUserById() {
        long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(userId);

        verify(userRepository).findById(userId);

        assertTrue(result.isPresent());
        assertEquals(user.getId(), result.get().getId());
    }

    @Nested
    class UpdateUserAvatarById {
        private final long userId = 1L;
        private final String avatarUrl = "avatarUrl";

        @Test
        void shouldThrowExceptionWhenUserDoesNotExist() {
            when(userRepository.updateUserAvatarById(anyLong(), anyString())).thenReturn(0);

            assertThrows(EntityDoesNotExistsException.class, () -> userService.updateUserAvatarById(userId, avatarUrl));

            verify(userRepository).updateUserAvatarById(userId, avatarUrl);
        }

        @Test
        void shouldMarkUserAsVerified() {
            when(userRepository.updateUserAvatarById(anyLong(), anyString())).thenReturn(1);

            userService.updateUserAvatarById(userId, avatarUrl);

            verify(userRepository).updateUserAvatarById(userId, avatarUrl);
        }
    }
}
