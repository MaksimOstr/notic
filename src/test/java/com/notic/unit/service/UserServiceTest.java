package com.notic.unit.service;

import com.notic.dto.CreateLocalUserDto;
import com.notic.dto.CreateProfileDto;
import com.notic.dto.CreateProviderUserDto;
import com.notic.dto.UserWithProfileDto;
import com.notic.entity.Profile;
import com.notic.entity.Role;
import com.notic.entity.User;
import com.notic.enums.AuthProviderEnum;
import com.notic.exception.EntityAlreadyExistsException;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.repository.UserRepository;
import com.notic.service.ProfileService;
import com.notic.service.RoleService;
import com.notic.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
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
    private ProfileService profileService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;


    @Nested
    class CreateLocalUser {
        private final String email = "test@gmail.com";
        private final String password = "testPassword";
        private final String username = "bob";
        private final String encodedPassword = "encoded";
        private final Role userRole = new Role("ROLE_USER");
        private final CreateLocalUserDto createUserDto = new CreateLocalUserDto(email, password, username);
        private final User createdUser = new User(createUserDto.email(), encodedPassword, Set.of(userRole));
        private final Profile profile = new Profile(
                createUserDto.username(),
                null,
                createdUser
        );


        @Test
        void successfullyCreateUser() {
            Role userRole = new Role("ROLE_USER");
            String encodedPassword = "encoded";
            User createdUser = new User(createUserDto.email(), encodedPassword, Set.of(userRole));

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleService.getDefaultRole()).thenReturn(userRole);
            when(profileService.createProfile(any(CreateProfileDto.class))).thenReturn(profile);
            when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
            when(userRepository.save(any(User.class))).thenReturn(createdUser);

            UserWithProfileDto result = userService.createUser(createUserDto);

            verify(roleService).getDefaultRole();
            verify(passwordEncoder).encode(createUserDto.password());
            verify(userRepository).save(createdUser);
            verify(userRepository).existsByEmail(createUserDto.email());

            Profile resultProfile = result.profile();
            User resultUser = result.user();

            assertNotNull(result);
            assertEquals(createUserDto.email(), resultUser.getEmail());
            assertEquals(encodedPassword, resultUser.getPassword());
            assertNotEquals(createUserDto.password(), resultUser.getPassword());
            assertEquals(resultProfile.getUsername(), createUserDto.username());
        }

        @Test
        void userAlreadyExists() {
            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            assertThrows(EntityAlreadyExistsException.class, () -> userService.createUser(createUserDto));

            verify(userRepository).existsByEmail(createUserDto.email());
            verifyNoInteractions(passwordEncoder, roleService, profileService);
            verifyNoMoreInteractions(userRepository);
        }

        @Test
        void dataIntegrityViolationException() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleService.getDefaultRole()).thenReturn(userRole);
            when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
            when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException(""));

            assertThrows(EntityAlreadyExistsException.class, () -> userService.createUser(createUserDto));
        }
    }


    @Nested
    class CreateProviderUser {
        AuthProviderEnum provider = AuthProviderEnum.GOOGLE;
        String email = "email";
        String username = "username";
        String avatar = "avatar";
        CreateProviderUserDto dto = new CreateProviderUserDto(
                provider,
                email,
                username,
                avatar
        );

        @Test
        void shouldReturnUser() {
            User user = new User();
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

            User result = userService.createProviderUser(dto);

            verify(userRepository).findByEmail(dto.email());
            verifyNoMoreInteractions(userRepository);
            verifyNoInteractions(roleService, passwordEncoder, profileService);

            assertEquals(user, result);
        }

        @Test
        void shouldReturnNewUser() {
            Role role = new Role("ROLE_USER");
            ArgumentCaptor<CreateProfileDto> captor = ArgumentCaptor.forClass(CreateProfileDto.class);
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            User user = new User();

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(roleService.getDefaultRole()).thenReturn(role);
            when(profileService.createProfile(any(CreateProfileDto.class))).thenReturn(new Profile());

            User result = userService.createProviderUser(dto);

            verify(userRepository).findByEmail(dto.email());
            verify(userRepository).save(userCaptor.capture());
            verify(profileService).createProfile(captor.capture());

            CreateProfileDto profileDto = captor.getValue();
            User savedUser = userCaptor.getValue();

            assertEquals(dto.avatar(), profileDto.avatar());
            assertEquals(dto.username(), profileDto.username());
            assertEquals(dto.email(), savedUser.getEmail());
            assertEquals(Set.of(role), savedUser.getRoles());
            assertEquals(user, result);
        }
    }


    @Test
    void getUserByEmail() {
        String email = "email";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByEmail(email);

        verify(userRepository).findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        assertEquals(email, result.get().getEmail());
    }


    @Test
    void getUserByEmailWithRoles() {
        String email = "test@gmail.com";
        String password = "12121212";
        Set<Role> roles = Set.of(new Role("ROLE_USER"));
        User user = new User(email, password, roles);

        when(userRepository.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByEmailWithRoles(email);

        verify(userRepository).findByEmailWithRoles(email);

        assertTrue(result.isPresent());

        User resultUser = result.get();

        assertNotNull(resultUser);
        assertEquals(user, resultUser);
        assertEquals(email, resultUser.getEmail());
        assertEquals(roles, resultUser.getRoles());
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
        assertEquals(user, result.get());
    }
}
