package com.surest.member.service;

import com.surest.member.dto.UserRequest;
import com.surest.member.dto.UserResponse;
import com.surest.member.entity.Role;
import com.surest.member.entity.User;
import com.surest.member.exception.BusinessServiceException;
import com.surest.member.repository.RoleRepository;
import com.surest.member.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequest userRequest;
    private Role role;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        role = Role.builder()
                .id(UUID.randomUUID())
                .name("ROLE_USER")
                .build();


        user = User.builder()
                .id(UUID.randomUUID())
                .username("john")
                .email("john@example.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .passwordHash("encodedpass")
                .roles(Set.of(role))
                .build();

        userRequest = UserRequest.builder()
                .username("john")
                .email("john@example.com")
                .password("secret")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .roles(Set.of("ROLE_USER"))
                .build();
    }

    //  getAllUsers
    @Test
    void getAllUsers_ShouldReturnMappedResponses() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> responses = userService.getAllUsers();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUsername()).isEqualTo("john");
        verify(userRepository).findAll();
    }

    // getUserById
    @Test
    void getUserById_ShouldReturnUser_WhenFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(user.getId());

        assertThat(response.getUsername()).isEqualTo("john");
        verify(userRepository).findById(user.getId());
    }

    @Test
    void getUserById_ShouldThrow_WhenNotFound() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // createUser
    @Test
    void createUser_ShouldSaveUser_WhenValid() throws BusinessServiceException {
        when(userRepository.existsByUsername(userRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encodedPass");
        when(roleRepository.findByNameIn(userRequest.getRoles())).thenReturn(Set.of(role));

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(UUID.randomUUID());
            return savedUser;
        });

        UserResponse response = userService.createUser(userRequest);

        assertThat(response.getUsername()).isEqualTo("john");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrow_WhenUsernameExists() {
        when(userRepository.existsByUsername(userRequest.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userRequest))
                .isInstanceOf(BusinessServiceException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_ShouldThrow_WhenEmailExists() {
        when(userRepository.existsByUsername(userRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userRequest))
                .isInstanceOf(BusinessServiceException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any());
    }

    // updateUser
    @Test
    void updateUser_ShouldUpdateFields_WhenUserExists() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserRequest update = UserRequest.builder()
                .email("new@example.com")
                .dateOfBirth(LocalDate.of(2000, 5, 15))
                .build();

        UserResponse response = userService.updateUser(user.getId(), update);

        assertThat(response.getEmail()).isEqualTo("new@example.com");
        assertThat(response.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 5, 15));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(UUID.randomUUID(), userRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // deleteUser
    @Test
    void deleteUser_ShouldCallRepository() {
        UUID id = UUID.randomUUID();
        userService.deleteUser(id);

        verify(userRepository).deleteById(id);
    }
}