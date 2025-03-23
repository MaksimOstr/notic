package com.notic.unit.service;

import com.notic.dto.CreateUserDto;
import com.notic.entity.Role;
import com.notic.entity.User;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.projection.UserCredentialsProjection;
import com.notic.repository.UserRepository;
import com.notic.service.RoleService;
import com.notic.service.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
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


            verify(roleService, times(1)).getDefaultRole();
            verify(passwordEncoder, times(1)).encode(createUserDto.password());
            verify(userRepository, times(1)).save(createdUser);
            verify(userRepository, times(1)).existsByEmail(createUserDto.email());



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

            verify(userRepository, times(1)).existsByEmail(createUserDto.email());
            verify(roleService, never()).getDefaultRole();
            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(anyString());
        }
    }




        @Nested
        class GetUserByEmail {
            private final String email = "test@gmail.com";
            private final String password = "12121212";
            private final User user = new User("test", email, password, Set.of(new Role("ROLE_USER")));


            @Test
            void userDoesNotExist() {
                when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

                Exception result = assertThrows(EntityDoesNotExistsException.class, () -> userService.getUserByEmail(email));

                assertEquals("User not found", result.getMessage());
            }

            @Test
            void UserExists() {
                when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

                User result = userService.getUserByEmail(email);


                assertNotNull(result);
                verify(userRepository, times(1)).findByEmail(email);
                assertEquals(user, result);
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

            Exception result = assertThrows(EntityDoesNotExistsException.class, () -> userService.getUserByEmailWithRoles(email));

            assertEquals("User not found", result.getMessage());
        }

        @Test
        void UserExists() {
            when(userRepository.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));

            User result = userService.getUserByEmailWithRoles(email);


            assertNotNull(result);
            verify(userRepository, times(1)).findByEmailWithRoles(email);
            assertEquals(user, result);
        }
    }

    @Nested
    class getUserForAuth {
        private final String email = "test@gmail.com";

        @Test
        void userDoesNotExist() {
            when(userRepository.findUserForAuthByEmail(anyString())).thenReturn(Optional.empty());

            Exception result = assertThrows(EntityDoesNotExistsException.class, () -> userService.getUserForAuth(email));

            assertEquals("User not found", result.getMessage());
        }

        @Test
        void UserExists() {

            UserCredentialsProjection projectionMock = mock(UserCredentialsProjection.class);
            when(userRepository.findUserForAuthByEmail(anyString())).thenReturn(Optional.of(projectionMock));

            UserCredentialsProjection result = userService.getUserForAuth(email);

            assertNotNull(result);
            verify(userRepository, times(1)).findUserForAuthByEmail(email);
        }
    }
}
